define(['app/bierapp/src/bierapp.module','next'], function(bierapp) {

  bierapp.directive('slideoutRight', function() {
    return {
      restrict: 'E',
      templateUrl: 'src/app/bierapp/src/templates/side-panel-index.tpl.html'
    };
  });
  
  bierapp.directive('biermanTopology', function() {
    return {
        'restrict': 'E',
        'template': '',
        'scope': {
            'topologyStartOver': '=',
            'topoInitialized': '=',
            'topo': '=',
            'openPanel': '=',
            'processBierTreeData': '=',
            'processDeployChannelData': '=',
            'resetTopology': '=',
            'highlightPath': '=',
            'convertUniToBiLinks': '='
        },
        'link': function($scope, iElm, iAttrs, controller){
            var initTopology;
            initTopology = function () {
                console.log('init nextUI ');

                $scope.dumpData = null;

                $scope.processDeployChannelData = function (successCbk, errorCbk) {
                    console.log('nextUI processDeployChannelData');
                    var tree = $scope.$parent.currentTree;
                    var ingress = $scope.topo.getNode(tree.ingress);
                    var input;
                    var errMsg;

                    // if a tree's ready
                    if ($scope.$parent.appConfig.currentTopologyId && ingress !== undefined && ingress !== null) {
                        console.log('egress Length=', tree.egress.length);

                        if (tree.egress.length !== 0) {
                            input = {
                                'input': {
                                    'topology-id': $scope.$parent.appConfig.currentTopologyId,
                                    'ingress-node': ingress.model()._data['node-id'],
                                    'egress-node': []
                                }
                            };

                            input.input['egress-node'] = tree.egress.map(function (egressId) {
                                var egress = $scope.topo.getNode(egressId);
                                return {
                                    'node-id': egress.model()._data['node-id']
                                };

                            });
                            successCbk(input);
                        }
                        else {
                            errMsg = 'You must specify the egress nodes for channel';
                            errorCbk(errMsg);
                        }
                    }
                    else {
                        errMsg = 'ingress node was not set properly. Try again.';
                        errorCbk(errMsg);
                    }
                };

                $scope.convertUniToBiLinks = function (uniLinks, clearLinks) {
                    console.log('nextUI convertUniToBiLinks');
                    clearLinks = clearLinks || false;
                    var biLinks = [];
                    var linksLayer = $scope.topo.getLayer('links');
                    linksLayer.eachLink(function (link) {
                        var linkContainer = link.model()._data.links;

                        for (var i = 0; i < uniLinks.length; i++) {
                            for (var j = 0; j < linkContainer.length; j++) {
                                if (uniLinks[i]['link-id'] == linkContainer[j].linkId) {
                                    biLinks.push(link.id());
                                }
                            }
                        }
                        if (clearLinks)
                            link.color($scope.colorTable.linkTypes.none);
                    });
                    return biLinks;
                };

                $scope.openPanel = function (panelCode, auxParam) {
                    console.log('nextUI open Panel');
                    var topoDiv = $('#bierman-topology');
                    var previousPanelType = $scope.$parent.appConfig.currentPanel;
                    $scope.topo.adaptToContainer();
                    $scope.topo.fit();
                    $scope.topo.resize(topoDiv.innerWidth, topoDiv.innerHeight);
                    //
                    //     $scope.$parent.appConfig.currentPanel = panelCode;
                    //     $scope.fadeInAllLayers();
                    //     if (panel.hasClass('visible') && previousPanelType == panelCode) { //user attempts to close slide-out
                    //     $scope.topo.getLayer('nodes').highlightedElements().clear(); //clears anything left highlighted
                    //     $scope.topo.getLayer('links').highlightedElements().clear();

                    //     panel.removeClass('visible').animate({'margin-right':'-400px'}); //shift slidepanel
                    //     $('div').find('.in').removeClass('in');
                    //     $scope.topo.adaptToContainer(); //fix topo size
                    //     } else {
                    //     panel.addClass('visible').animate({'margin-right': '0px'}); //shifts slidepanel
                    //     $scope.topo.resize((window.innerWidth - 200), 0.975 * (window.innerHeight)); //resize topology
                    //     $scope.topo.fit(); //fits to view
                    //     }
                    //
                };

                // highlights a node
                $scope.highlightNode = function (targetId, noLinks) {
                    var nodeLayer = $scope.topo.getLayer('nodes');
                    var linksLayer = $scope.topo.getLayer('links');
                    var linksLayerHighlightElements = linksLayer.highlightedElements();
                    var nodeLayerHighlightElements = nodeLayer.highlightedElements();

                    noLinks = noLinks || false;
                    //Clears previous
                    nodeLayerHighlightElements.clear();
                    linksLayerHighlightElements.clear();

                    //highlight nodes
                    nodeLayerHighlightElements.add($scope.topo.getNode(targetId));
                    if (!noLinks) {
                        //highlight links
                        linksLayerHighlightElements.addRange(nx.util.values($scope.topo.getNode(targetId).links()));
                    }
                    else {
                        linksLayer.fadeOut(true);
                    }
                };

                $scope.highlightPath = function (links) {
                    links.forEach(function (linkId) {
                        var link = $scope.topo.getLink(linkId);
                        link.color($scope.colorTable.linkTypes.path);

                    });
                };

                $scope.resetTopology = function () {
                    $scope.applyChanges();
                };

                // highlights a link
                $scope.highlightLink = function (linkId) {
                    var nodeLayer = $scope.topo.getLayer('nodes');
                    var linksLayer = $scope.topo.getLayer('links');
                    var linksLayerHighlightElements = linksLayer.highlightedElements();
                    var nodeLayerHighlightElements = nodeLayer.highlightedElements();
                    var link = $scope.topo.getLink(linkId);

                    //Clears previous
                    nodeLayerHighlightElements.clear();
                    linksLayerHighlightElements.clear();

                    //highlight link
                    linksLayerHighlightElements.add(link);
                    //highlight connected nodes
                    nodeLayerHighlightElements.addRange(nx.util.values({
                        source: $scope.topo.getNode(link.model().sourceID()),
                        target: $scope.topo.getNode(link.model().targetID())
                    }));
                };

                // completely clear all paths from path layer
                $scope.clearPathLayer = function () {
                    var pathLayer = $scope.topo.getLayer("paths");
                    pathLayer.clear();
                    return pathLayer;
                };

                $scope.getNodeTypeById = function (id) {
                    if ($scope.$parent.currentTree.ingress == id)
                        return 'ingress';
                    else if ($scope.$parent.currentTree.egress.indexOf(id) != -1)
                        return 'egress';
                    else
                        return 'none';
                };

                $scope.getLinkTypeById = function (id) {
                    if ($scope.$parent.currentTree.links.indexOf(id) != -1)
                        return 'path';
                    else
                        return 'none';
                };

                $scope.fadeInAllLayers = function () {
                    //fade out all layers
                    nx.each($scope.topo.layers(), function (layer) {
                        layer.fadeIn(true);
                    }, this);
                };

                $scope.fadeOutAllLayers = function () {
                    //fade out all layers
                    nx.each($scope.topo.layers(), function (layer) {
                        layer.fadeOut(true);
                    }, this);
                };

                $scope.applyChanges = function () {
                    var nodes = $scope.topo.getLayer('nodes');
                    var links = $scope.topo.getLayer('links');
                    // apply changes to nodes
                    nodes.eachNode(function (node) {
                        node.applyChanges();
                    });
                    // apply changes to links
                    links.eachLink(function (link) {
                        link.applyChanges();
                    });
                };

                $scope.pickNode = function (id) {
                    // select source
                    console.log('pick node');
                    if ($scope.$parent.appConfig.mode == 'start') {
                        $scope.$parent.currentTree.ingress = id;
                        console.log('ingress=' + $scope.$parent.currentTree.ingress);
                        $scope.$parent.appConfig.mode = 'draw';
                    }
                    // select receivers
                    else if ($scope.$parent.appConfig.mode == 'draw') {
                        var nodeIndex = $scope.$parent.currentTree.egress.indexOf(id);
                        // if node is not used
                        if (nodeIndex == -1 && id != $scope.$parent.currentTree.ingress){
                            $scope.$parent.currentTree.egress.push(id);
                            console.log('egress=' + $scope.$parent.currentTree.egress);
                        }
                        // if node is ingress
                        else if (id == $scope.$parent.currentTree.ingress) {
                            $scope.$parent.currentTree.ingress = null;
                            $scope.$parent.appConfig.mode = 'start';
                        }
                        // if the node is egress
                        else if (nodeIndex > -1) {
                            $scope.$parent.currentTree.egress.splice(nodeIndex, 1);//splice()
                            //console.log()
                        }
                    }
                    $scope.$parent.currentTree.validStatus = 'none';
                    $scope.$apply();
                    $scope.applyChanges();
                };

                $scope.pickLink = function (id) {
                    console.log('pick link');
                    if ($scope.$parent.appConfig.mode == 'draw') {
                        var indexOfLink = $scope.$parent.currentTree.links.indexOf(id);
                        if ($scope.$parent.currentTree.links.indexOf(id) == -1) {
                            $scope.$parent.currentTree.links.push(id);
                        }
                        else {
                            $scope.$parent.currentTree.links.splice(indexOfLink, 1);
                        }
                        $scope.$parent.currentTree.validStatus = 'none';
                        $scope.$apply();
                        $scope.applyChanges();
                    }
                };

            };
            console.log('next UI init');
            $scope.colorTable = {
                'nodeTypes': {
                    'ingress': '#009933',      //green
                    'egress': '#0033cc',       //blue
                    'none': '#0591D9'

                },
                'linkTypes': {
                    'path': '#009933',   //green
                    'none': '#67C9E4'
                }
            };

            nx.define('CustomScene', nx.graphic.Topology.DefaultScene, {
                'methods': {
                    clickNode: function (topology, node) {
                        $scope.pickNode(node.id());
                    },
                    clickLink: function (topology, link) {
                        $scope.pickLink(link.id());
                    }
                }
            });

            nx.define('ExtendedNode', nx.graphic.Topology.Node, {
                'methods': {
                    'init': function (args) {
                        this.inherited(args);
                        var stageScale = this.topology().stageScale();
                        this.view('label').setStyle('font-size', 14 * stageScale);
                    },
                    'setModel': function (model) {
                        this.inherited(model);
                    },
                    'applyChanges': function () {
                        var type = $scope.getNodeTypeById(this.id());
                        if ($scope.colorTable.nodeTypes.hasOwnProperty(type)) {
                            this.color($scope.colorTable.nodeTypes[type]);
                        }
                    }
                }
            });

            nx.define('ExtendedLink', nx.graphic.Topology.Link, {
                'methods': {
                    'init': function (args) {
                        this.inherited(args);
                        // fixme: third parameter should be false
                        $scope.topo.fit(undefined, undefined, true);
                    },
                    'setModel': function (model) {
                        this.inherited(model);
                    },
                    'applyChanges': function () {
                        var type = $scope.getLinkTypeById(this.id());
                        if ($scope.colorTable.linkTypes.hasOwnProperty(type)) {
                            this.color($scope.colorTable.linkTypes[type]);
                        }
                    }
                }
            });

            $scope.topo = new nx.graphic.Topology({
                'adaptive': true,  // width 100% if true
                'scalable': true,  // enable scaling
                'showIcon': true,  // show icons' nodes, otherwise display dots
                'nodeConfig': {
                    'label': 'model.name',  //'model.attributes.name'
                    'iconType': 'router',
                    'color': $scope.colorTable.nodeTypes.none
                },
                'linkConfig': {
                    //'label': 'model.id',
                    'label': 'model.linkId',
                    'linkType': 'curve',
                    'width': 5,
                    'color': $scope.colorTable.linkTypes.none
                },
                'nodeSetConfig': {
                    'label': 'model.name',
                    'iconType': 'router'
                },
                'identityKey': 'id',  // property name to identify unique nodes，  helps to link source and target
                'enableSmartLabel': true,   // moves the labels in order to avoid overlay
                'enableSmartNode': true,   // moves the node in order to avoid overlay
                'enableGradualScaling': true,   // smooth scaling. may slow down, if true
                'supportMultipleLink': true,    // if true, two nodes can have more than one link
                'dataProcessor': 'force',     //automatically compute the position of nodes
                //'dataProcessor': 'Quick',     //automatically compute the position of nodes
                'autoLayout': true,
                'nodeInstanceClass': 'ExtendedNode',
                'linkInstanceClass': 'ExtendedLink',
                //'layoutType': 'USMap',
                'layoutConfig': {
                    'longitude': 'model._data.longitude',
                    'latitude': 'model._data.latitude'
                }
            });
            // fired when topology is generated
            $scope.topo.on('topologyGenerated', function (sender, event) {
                // use custom events for the topology
                sender.registerScene('ce', 'CustomScene');   //Register a scene to topology
                sender.activateScene('ce');   //Activate a scene, topology only has one active scene.
                // enable tooltips for both nodes and links
                $scope.topo.tooltipManager().showNodeTooltip(true);
                $scope.topo.tooltipManager().showLinkTooltip(true);
                $scope.topo.adaptToContainer();
            });


            $scope.topo.on('fitStage', function (sender, event) {
                setTimeout (function () {
                    if ($scope.$parent.appConfig.mode == 'init') {
                        $scope.$parent.appConfig.mode = 'start';
                        $scope.$apply();
                    }
                }, 1000);
            });

            var app = new nx.ui.Application();    // instantiate NeXt app
            // app run in container. In our case this is the one with id="bierman-topology"
            app.container(document.getElementById('bierman-topology'));
            app.on('resize', function () {
                $scope.topo.adaptToContainer();

            });
            $scope.topo.attach(app);

            //};
            $scope.$watch('$parent.showTopologyData', function(){
                //if($scope.$parent.showTopologyData.nodes.length && $scope.$parent.topoInitialized === true) {
                if($scope.$parent.topoInitialized === true) {
                    console.log('topology update');
                    //console.log('$scope.$parent.showTopologyData',$scope.$parent.showTopologyData);
                    $scope.topo.data($scope.$parent.showTopologyData);
                    initTopology($scope.$parent.showTopologyData);
                }
            });
        }
    };
  });
  

});
