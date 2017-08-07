define([
	'app/bierapp/src/bierapp.module',
	'app/bierapp/src/bierapp.service',
	'app/bierapp/src/bierapp.directives',
	'app/bierapp/src/bierapp.filter',
	'next',
	'tools',
	'sweetalert'
	], function(bierapp) {

  bierapp.controller('biermanCtrl', ['$scope', '$rootScope', 'BiermanRest', '$mdSidenav', '$mdDialog', '$mdMedia',
		function($scope, $rootScope, BiermanRest, $mdSidenav, $mdDialog, $mdMedia) {
   
	$rootScope['section_logo'] = 'src/app/bierapp/assets/images/bier.jpg';
	
	$scope.appConfig = {
		// FOR MANUAL CONFIGURATION
		'ipAutoDetection': true, // automatically substitute proxyHost with hostname in browser
		'httpMaxTimeout': 10000, // Maximum timeout in milliseconds for HTTP requests
		'maxPacketLoss': 10,

		// DO NOT MODIFY CONFIGURATION BELOW
		'mode': 'init', // Application mode (do not modify)
		'queryTopology' : 'no',
		'topoInitialized': false,
		'currentPanel': 'topology-manager',
		'currentTopologyId': null,
		'currentDomain': null
	};
	// dynamic replacement of proxy hostname

	//var BiermanRest = new BiermanRest1($scope.appConfig);
 
	$scope.bitstringlength = [
		{ "bsl" : "64-bit"},
		{ "bsl" : "128-bit"},
		{ "bsl": "256-bit"},
		{ "bsl" : "512-bit"},
		{ "bsl" : "1024-bit"},
		{ "bsl" : "2048-bit"},
		{ "bsl" : "4096-bit"}
	];
	$scope.ipv4bitstringlength = [
		{ "bsl" : "64"},
		{ "bsl" : "128"},
		{ "bsl" : "256"},
		{ "bsl" : "512"},
		{ "bsl" : "1024"},
		{ "bsl" : "2048"},
		{ "bsl" : "4096"}
	];

	$scope.igpType = [
		{ "type" : "OSPF"},
		{ "type" : "ISIS"}
	];

	$scope.topologyData = null;

	$scope.netconfNode = [];

	$scope.bierTeLabelRange = [];

	$scope.channelData = null;

	$scope.topologyMergerData  = {    //$scope.processTopologyData use this
		'nodes': [],
		'links': [],
		// external id -> internal id
		nodesDict: new nx.data.Dictionary({}),
		linksDict: new nx.data.Dictionary({})
	};

	$scope.subdomaintopologyData  = {
		'nodes': [],
		'links': [],
		'nodeSet':[],
		// external id -> internal id
		nodesDict: new nx.data.Dictionary({}),
		linksDict: new nx.data.Dictionary({})
	};

	$scope.currentTree = {
		'ingress': null,
		'egress': [],
		'links': [],
		'validStatus': 'none',
		'deploymentStatus': 'none',
		'fmask': '',
		'processedTree': {}
	};

	$scope.topology = null;

	$scope.nodelength = 0;

	$scope.linklength = 0;

	$scope.domainData = null;   //query domian and subdomain data, then process them into $scope.domainData

	$scope.subdomainData = null;    //query subdomain data,this data will process into $scope.domainData

	$scope.topologyidData = null;           //topology id

	$scope.nodeForDeployChannel = [];

	//websocket topo-change setTimeout
	$scope.first = true;
	$scope.next = false;
	$scope.timeout = false;

	//scope for LoadDomainTopology
	$scope.domianTopology = {
		'domain':null,
		'subdomain':null,
		'subdomainCount':0,
		'subdomainTopoLength':0,
		'processSubdomainNodeNum':0,
		'processSubdomainLinkNum':0,
		'subdomainNodeIndex':0,
		'subdomainLinkIndex':0
	};
	//scope for LoadTeDomainTopology
	$scope.teTopology = {
		'teDomain':null,
		'teSubdomain':null,
		'teSubdomainCount':0,
		'tesubdomainTopoLength':0,
		'processteSubdomainNodeNum':0,
		'processteSubdomainLinkNum':0,
		'teSubdomainNodeIndex':0,
		'teSubdomainLinkIndex':0
	};

	$scope.queryTeSubdomainNum = 0;
	$scope.returnTeSubdomainNum = 0;
	$scope.mergeTeSubDomainNodeData = [];

	//Log for notifcation
	$scope.log = [];
	$scope.logCounter = 0;

	//error message
	$scope.errMsg1 = "failed . You must specify each parameter.";
	$scope.errMsg2 = "Failed to configued a node. You must specify parameters of domain,subdomain,ipv4-bitstringlength and ipv4-bier-mpls-label-base.";
	$scope.warning1 = "BGP-LS Not configure";

	$scope.compare = function (prop) {
		return function (obj1, obj2) {
			var val1 = obj1[prop];
			var val2 = obj2[prop];
			if (!isNaN(Number(val1)) && !isNaN(Number(val2))) {
				val1 = Number(val1);
				val2 = Number(val2);
			}
			if (val1 < val2) {
				return -1;
			} else if (val1 > val2) {
				return 1;
			} else {
				return 0;
			}
		};
	};

	$scope.displayAlert = function(options){
		swal(options);
	};

	$scope.clearSubdomaintopologyData = function(){
		$scope.subdomaintopologyData  = {
			'nodes': [],
			'links': [],
			'nodeSet':[],
			nodesDict: new nx.data.Dictionary({}),
			linksDict: new nx.data.Dictionary({})
		};
	};

	$scope.clearCurrentTopologyMergerData = function(){
		$scope.topologyMergerData  = {
			'nodes': [],
			'links': [],
			// external id -> internal id
			nodesDict: new nx.data.Dictionary({}),
			linksDict: new nx.data.Dictionary({})
		};
	};

	$scope.clearShowTopologyData = function(){
		$scope.showTopologyData = {
			'nodes' : [],
			'links' : []
		};
	};

	$scope.clearQueryTopology = function(){
		$scope.topoDate = {
			'node': [],
			'link': []
		};
	};

	$scope.clearCurrentTree = function(){
		$scope.currentTree = {
			'ingress': null,
			'egress': [],
			'links': [],
			'validStatus': 'none',
			'deploymentStatus': 'none',
			'fmask': '',
			'processedTree': {}
		};
	};

	//change domainData to suddomainData
	$scope.changeDomainData =function(select){
		console.log('change  select', select);

		//biermanTools.hasOwnProperties
		if(select !== undefined && biermanTools.hasOwnProperties(select,['subdomain'])){
			$scope.subdomainData = select.subdomain;
			console.log('has');
		}
	};

	$scope.clearCurrentDomainData = function(){
		$scope.domainData = [];
	};

	$scope.registerNotificationId = function(){
		BiermanRest.registerNotificationId(

			{
				"input":{
					"notifications": [
						"(urn:bier:topology:api?revision=2016-11-02)topo-change",
						"(urn:bier:service:api?revision=2017-01-05)report-message",
						//"(urn:bier:driver:reporter?revision=2017-02-13)driver-failure"
					]
				}
				
			},
			function(data){
				$scope.websocketLocation(data);
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Notification identifier not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.websocketLocation = function(id){
		BiermanRest.websocketLocation(
			id,
			function(data){
				$scope.websocket(data);
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Web Location not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.websocket = function(location){
		var ws = new WebSocket(location);
		ws.onopen = function() {
			ws.send("I'm BIER app client");
		};

		ws.onmessage = function (evt) {
			console.log(evt.data);
			var domParser = new DOMParser();
			var xmlDoc = domParser.parseFromString(evt.data, 'application/xml');
			var topo =xmlDoc.getElementsByTagName("topo-change");

			if(topo.length > 0){
				var topoid =xmlDoc.getElementsByTagName("topo-id")[0].childNodes[0].nodeValue;
				if($scope.first === false && $scope.timeout === true){
					$scope.next = true;
				}
				if($scope.first === true){
					$scope.first = false;
					$scope.displayAlert({
						title: "Topology changed",
						text: "Topology " + topoid + " changed",
						timer: 2000,
						confirmButtonText: "Close"
					});
					if ($scope.appConfig.currentTopologyId == topoid){
						setTimeout(function(){
							$scope.queryTopology(topoid);
						}, 2000);
						setTimeout(function(){
							$scope.timeout = true;
						}, 4000);
						setTimeout(function(){
							if ($scope.next === true ) {
								$scope.queryTopology(topoid);
							}
							$scope.first = true;
							$scope.next = false;
							$scope.timeout = false;
						}, 10000);
					}
					else{
						setTimeout(function(){
							$scope.first = true;
							$scope.next = false;
							$scope.timeout = false;
						}, 10000);
					}
				}
			}

			var report =xmlDoc.getElementsByTagName("report-message");
			if(report.length > 0){
				var time1 =xmlDoc.getElementsByTagName("eventTime")[0].childNodes[0].nodeValue;
				var reason1 =xmlDoc.getElementsByTagName("failure-reason")[0].childNodes[0].nodeValue;
				var content1 = {};
				content1.message = '[#' + $scope.logCounter + '] ' + reason1;
				content1.time = time1;
				$scope.log.push(content1);
				$scope.logCounter++;
				setTimeout(function(){
					$scope.displayAlert({
						title: "Error Notification",
						text: reason1,
						type: "error",
						showConfirmButton: true,
						confirmButtonText: "Close"
					});}, 2000);

			}

			var driverFailure =xmlDoc.getElementsByTagName("driver-failure");
			if(driverFailure.length > 0){
				var time2 =xmlDoc.getElementsByTagName("eventTime")[0].childNodes[0].nodeValue;
				var reason2 =xmlDoc.getElementsByTagName("failure-message")[0].childNodes[0].nodeValue;
				var content2 = {};
				content2.message = '[#' + $scope.logCounter + '] ' + reason2;
				content2.time = time2;
				$scope.log.push(content2);
				$scope.logCounter++;
				$scope.displayAlert({
					title: "Error Notification",
					text: reason2,
					type: "error",
					showConfirmButton: true,
					confirmButtonText: "Close"
				});
			}
		};

		ws.onclose = function() {
			//alert("BIER Controller Websocket Closed");
		};

		ws.onerror = function(err) {
			//alert("Error: " + err);
			$scope.displayAlert({
				title: "Notifications error",
				text: "notifications connect to " + location +" error",
				type: "error",
				confirmButtonText: "Close"
			});
		};
	};

	$scope.clearLog = function(){
		$scope.log = [];
		$scope.logCounter = 0;
	};

	$scope.configBGP = function(){
		if(biermanTools.hasOwnProperties($scope.appConfig, ['bgpRouter', 'bgpAs', 'bgpNeighborId', 'bgpPeerAs'])) {
			BiermanRest.configBGP(
				{
					"network-instances": {
						"network-instance": [
							{
								"name": "global-bgp",
								"protocols": {
									"protocol": [
										{
											"identifier": "openconfig-policy-types:BGP",
											"name": "example-bgp-rib",
											"bgp-openconfig-extensions:bgp": {
												"global": {
													"config": {
														"as": $scope.appConfig.bgpAs,
														"router-id": $scope.appConfig.bgpRouter
													},
													"afi-safis": {
														"afi-safi": [
															{
																"afi-safi-name": "openconfig-bgp-types:L2VPN-EVPN"
															},
															{
																"afi-safi-name": "bgp-openconfig-extensions:IPV6-FLOW"
															},
															{
																"afi-safi-name": "openconfig-bgp-types:L3VPN-IPV6-UNICAST"
															},
															{
																"afi-safi-name": "bgp-openconfig-extensions:IPV4-L3VPN-FLOW"
															},
															{
																"afi-safi-name": "openconfig-bgp-types:IPV6-UNICAST"
															},
															{
																"afi-safi-name": "bgp-openconfig-extensions:IPV4-FLOW"
															},
															{
																"afi-safi-name": "openconfig-bgp-types:IPV4-UNICAST"
															},
															{
																"afi-safi-name": "bgp-openconfig-extensions:LINKSTATE"
															},
															{
																"afi-safi-name": "openconfig-bgp-types:IPV4-LABELLED-UNICAST"
															},
															{
																"afi-safi-name": "bgp-openconfig-extensions:IPV6-L3VPN-FLOW"
															},
															{
																"afi-safi-name": "openconfig-bgp-types:IPV6-LABELLED-UNICAST"
															},
															{
																"afi-safi-name": "openconfig-bgp-types:L3VPN-IPV4-UNICAST"
															}
														]
													}
												},
												"neighbors": {
													"neighbor": [
														{
															"neighbor-address": $scope.appConfig.bgpNeighborId,
															"timers": {
																"config": {
																	"keepalive-interval": 30,
																	"hold-time": 180,
																	"connect-retry": 10,
																	"minimum-advertisement-interval": 30
																}
															},
															"route-reflector": {
																"config": {
																	"route-reflector-client": false
																}
															},
															"transport": {
																"config": {
																	"mtu-discovery": false,
																	"passive-mode": false
																}
															},
															"config": {
																"peer-as": $scope.appConfig.bgpPeerAs,
																"peer-type": "INTERNAL",
																"send-community": "NONE",
																"route-flap-damping": false
															},
															"afi-safis": {
																"afi-safi": [
																	{
																		"afi-safi-name": "openconfig-bgp-types:L2VPN-EVPN"
																	},
																	{
																		"afi-safi-name": "bgp-openconfig-extensions:IPV6-FLOW"
																	},
																	{
																		"afi-safi-name": "openconfig-bgp-types:L3VPN-IPV6-UNICAST"
																	},
																	{
																		"afi-safi-name": "bgp-openconfig-extensions:IPV4-L3VPN-FLOW"
																	},
																	{
																		"afi-safi-name": "openconfig-bgp-types:IPV6-UNICAST"
																	},
																	{
																		"afi-safi-name": "bgp-openconfig-extensions:IPV4-FLOW"
																	},
																	{
																		"afi-safi-name": "openconfig-bgp-types:IPV4-UNICAST"
																	},
																	{
																		"afi-safi-name": "bgp-openconfig-extensions:LINKSTATE"
																	},
																	{
																		"afi-safi-name": "openconfig-bgp-types:IPV4-LABELLED-UNICAST"
																	},
																	{
																		"afi-safi-name": "bgp-openconfig-extensions:IPV6-L3VPN-FLOW"
																	},
																	{
																		"afi-safi-name": "openconfig-bgp-types:IPV6-LABELLED-UNICAST"
																	},
																	{
																		"afi-safi-name": "openconfig-bgp-types:L3VPN-IPV4-UNICAST"
																	}
																]
															}
														}


													]
												}
											}
										}
									]
								}
							}
						]
					}
				},
				// success
				function (data) {
					$scope.displayAlert({
						title: "Configure BGP Success",
						text: "BGP router-id and As configure success",
						type: "success",
						timer: 1000,
						confirmButtonText: "Okay"
					});
					$scope.configBierBGP();
				},
				// error
				function (err) {
					console.error(err);
					$scope.displayAlert({
						title: "Configure Failed",
						text: err.errMsg,
						type: "error",
						confirmButtonText: "Close"
					});
				}
			);
		}
		else{
			$scope.displayAlert({
				title: "Configure Failed",
				text: "config BGP " + $scope.errMsg1,
				type: "error",
				confirmButtonText: "Close"
			});
		}
	};

	$scope.configBierBGP = function(){
		if(biermanTools.hasOwnProperties($scope.appConfig, ['bgpRouter', 'bgpAs', 'bgpNeighborId', 'bgpPeerAs'])) {
			BiermanRest.configBierBGP(
				{
					"config":
					{
						"local":
						{
							"address": $scope.appConfig.bgpRouter,
							"as": $scope.appConfig.bgpAs
						},
						"neighbour":
						{
							"address": $scope.appConfig.bgpNeighborId,
							"as": $scope.appConfig.bgpPeerAs
						}
					}
				},
				// success
				function (data) {
					$scope.displayAlert({
						title: "Configure Bier BGP Success",
						text: "BGP router-id and As save to system success",
						type: "success",
						timer: 1000,
						confirmButtonText: "Okay"
					});
				},
				// error
				function (err) {
					console.error(err);
					$scope.displayAlert({
						title: "Save BGP To System Failed",
						text: err.errMsg,
						type: "error",
						confirmButtonText: "Close"
					});
				}
			);
		}
		else{
			$scope.displayAlert({
				title: "Configure Failed",
				text: "config BGP " + $scope.errMsg1,
				type: "error",
				confirmButtonText: "Close"
			});
		}
	};

	$scope.getBGPConfig = function(){
		BiermanRest.getBGPConfig(
			function(data){
				$scope.appConfig.bgpAs = data['network-instance'][0].
					protocols.protocol[0]['bgp-openconfig-extensions:bgp'].global.config.as;
				$scope.appConfig.bgpRouter = data['network-instance'][0].
					protocols.protocol[0]['bgp-openconfig-extensions:bgp'].global.config['router-id'];
				$scope.appConfig.bgpNeighborId = data['network-instance'][0].
					protocols.protocol[0]['bgp-openconfig-extensions:bgp'].neighbors.neighbor[0]['neighbor-address'];
				$scope.appConfig.bgpPeerAs = data['network-instance'][0].
					protocols.protocol[0]['bgp-openconfig-extensions:bgp'].neighbors.neighbor[0].config['peer-as'];
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Get BGP Configure Failed",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.getBierBGPConfig = function(){
		BiermanRest.getBierBGPConfig(
			function(data){
				if (data != 'null'){
					$scope.appConfig.bierbgpAs = data.local.as;
					$scope.appConfig.bierbgpRouter = data.local.address;
					$scope.appConfig.bierbgpNeighborId = data.neighbour.address;
					$scope.appConfig.bierbgpPeerAs = data.neighbour.as;
					$scope.checkBGP();
				}
				else {
					$scope.displayAlert({
						title: "Get BGP Configure Failed",
						text: $scope.warning1,
						type: "warning",
						confirmButtonText: "Close"
					});
				}

			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Get BGP Configure Failed",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.checkBGP = function(){
		if($scope.appConfig.bgpAs !== $scope.appConfig.bierbgpAs ||
			$scope.appConfig.bgpRouter !== $scope.appConfig.bierbgpRouter ||
			$scope.appConfig.bgpNeighborId !== $scope.appConfig.bierbgpNeighborId ||
			$scope.appConfig.bgpPeerAs !==  $scope.appConfig.bierbgpPeerAs){
			$scope.appConfig.bgpAs = $scope.appConfig.bierbgpAs;
			$scope.appConfig.bgpRouter = $scope.appConfig.bierbgpRouter;
			$scope.appConfig.bgpNeighborId = $scope.appConfig.bierbgpNeighborId;
			$scope.appConfig.bgpPeerAs =  $scope.appConfig.bierbgpPeerAs;
			$scope.configBGP();
		}
	};

	$scope.LoadTopology = function(){
		$scope.clearCurrentTree();
		BiermanRest.loadTopology(
			function(data){
				$scope.topologyidData = data;
				//$scope.registerNotificationId();
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Topology id not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.getNetconf = function(){
		$scope.netconfNode = $scope.topologyData.nodes;
		BiermanRest.getNetconf(
			function(data){
				console.log('nettopologydata',data);
				$scope.processNetconfNode(data);
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Netconf Nodes not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.processNetconfNode = function(node){
		if(node.hasOwnProperty('node')){
			var nodes = node.node;
			for(var i = 0; i< $scope.netconfNode.length; i++){
				$scope.netconfNode[i].ip = null;
				$scope.netconfNode[i].port = null;
				$scope.netconfNode[i].status = null;
			}
			console.log('nodes',nodes);
			for(var nodeIndex = 0; nodeIndex < nodes.length; nodeIndex++){
				for(var netconfIndex = 0; netconfIndex < $scope.netconfNode.length; netconfIndex++){
					if(nodes[nodeIndex]["node-id"] === $scope.netconfNode[netconfIndex]["node-id"]){
						$scope.netconfNode[netconfIndex].ip = nodes[nodeIndex]["netconf-node-topology:host"];
						$scope.netconfNode[netconfIndex].port = nodes[nodeIndex]["netconf-node-topology:port"];
						$scope.netconfNode[netconfIndex].status = nodes[nodeIndex]["netconf-node-topology:connection-status"];
						break;
					}
				}
			}
			$scope.netconfNode.sort($scope.compare("node-id"));
			console.log('$scope.netconfNode',$scope.netconfNode);
		}
	};

	$scope.getBierTeLabelRange = function(){
		if($scope.topology.hasOwnProperty('node-id')) {
			var nodelength = $scope.topology['node-id'].length;
			var nodeId = [];
			//console.log('$scope.topology', $scope.topology);
			for (var i = 0; i < nodelength; i++) {
				nodeId.push($scope.topology['node-id'][i]['node-id']);
			}

			BiermanRest.queryNode(
				{
					'topo-id': $scope.appConfig.currentTopologyId,
					'node': nodeId
				},
				//
				function(data){
					console.log('getBierTeLabelRange',data);
					$scope.processBierTeLabelRange(data.node);
					//$scope.processNetconfNode(data);
				},
				function(err){
					console.error(err);
					$scope.displayAlert({
						title: "Bier Te Label Range Nodes not loaded",
						text: err.errMsg,
						type: "error",
						confirmButtonText: "Close"
					});
				}
			);


		}

	};

	$scope.processBierTeLabelRange = function(node){
		$scope.bierTeLabelRange = [];
		for(var i = 0; i < node.length; i++){
			var bierTeLabelNode = {
				'nodeId': null,
				'labelBase': null,
				'labelRangeSize':null
			};
			//$scope.bierTeLabelRange
			bierTeLabelNode.nodeId = node[i]['node-id'];
			if(node[i].hasOwnProperty('bier-te-lable-range')){
				bierTeLabelNode.labelBase = node[i]['bier-te-lable-range']['label-base'];
				bierTeLabelNode.labelRangeSize = node[i]['bier-te-lable-range']['label-range-size'];
			}
			$scope.bierTeLabelRange.push(bierTeLabelNode);

		}
		if($scope.bierTeLabelRange.length > 0)
			$scope.bierTeLabelRange.sort($scope.compare("nodeId"));
		console.log('$scope.bierTeLabelRange', $scope.bierTeLabelRange);
	};

	$scope.LoadDomainTopology = function(domain){
		var hasSubdomain = true;
		$scope.domianTopology.domain = domain;
		for(var i = 0; i < $scope.domainData.length; i++){
			if($scope.domianTopology.domain === $scope.domainData[i]['domain-id']){
				$scope.domianTopology.subdomain = $scope.domainData[i].subdomain;
				if($scope.domianTopology.subdomain.length === 0){
					$scope.displayAlert({
						title: "No Subdomain",
						text: "domain " + domain + " has no subdomain",
						type: "error",
						confirmButtonText: "Close"
					});
					hasSubdomain = false;
				}
				break;
			}
		}
		if(hasSubdomain){
			$scope.domianTopology.subdomainTopoLength = $scope.domianTopology.subdomain.length;
			$scope.LoadSubdomainNode($scope.domianTopology.domain, $scope.domianTopology.subdomain[$scope.domianTopology.subdomainCount]['sub-domain-id']);
		}
	};

	//load subdomain node
	$scope.LoadSubdomainNode = function(domain, subdomain){
		BiermanRest.querySubdomainNode(
			{
				'topology-id': $scope.appConfig.currentTopologyId,
				'domain-id': domain,
				'sub-domain-id': subdomain
			},
			function(data){
				$scope.processSubdomainTopology(subdomain, data, 'node');
				$scope.LoadSubdomainLink($scope.domianTopology.domain, $scope.domianTopology.subdomain[$scope.domianTopology.subdomainCount]['sub-domain-id']);
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Subdomain nodes not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	//load subdomain link
	$scope.LoadSubdomainLink = function(domain, subdomain){
		BiermanRest.querySubdomainLink(
			{
				'topology-id': $scope.appConfig.currentTopologyId,
				'domain-id': domain,
				'sub-domain-id': subdomain
			},
			function(data){
				$scope.processSubdomainTopology(subdomain, data, 'link');
				$scope.domianTopology.subdomainCount++;
				if($scope.domianTopology.subdomainCount < $scope.domianTopology.subdomain.length){
						$scope.LoadSubdomainNode($scope.domianTopology.domain, $scope.domianTopology.subdomain[$scope.domianTopology.subdomainCount]['sub-domain-id']);
				}
				else{
					$scope.domianTopology.domain = null;
					$scope.domianTopology.subdomain = null;
					$scope.domianTopology.subdomainCount = 0;
				}
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Subdomain links not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	//process subdomain topology
	$scope.processSubdomainTopology = function(subdomain, data, flag){
		//console.log('process Subdomain Topology', data);
		function getKey(a, b){
			if(a < b)
				return a + '-' + b;
			else
				return b + '-' + a;
		}

		if(flag == 'node'){
			$scope.domianTopology.processSubdomainNodeNum++;
			if(data.length !== 0){
				if(data.hasOwnProperty('subdomain-node')) {
					var arr = [];
					for(var nodeIndexBase = 0; nodeIndexBase < data['subdomain-node'].length; nodeIndexBase++){
						var currentNode = data['subdomain-node'][nodeIndexBase];
						var node = {};
						// Internal ID
						node.id = $scope.domianTopology.subdomainNodeIndex;
						arr.push(node.id);
						$scope.domianTopology.subdomainNodeIndex++;
						// Global ID
						node['node-id'] = currentNode['node-id'];
						node.name = currentNode['node-id'];
						// BFR local id
						//node.bfrLocalId = currentNode['topology-bier:bfr-local-id'];
						// Router ID
						//node.routerId = currentNode['topology-bier:router-id'];
						// Assign node's external id to the internal one
						$scope.subdomaintopologyData.nodesDict.setItem(node['node-id'], node.id);
						// Record node data
						$scope.subdomaintopologyData.nodes.push(node);

					}
					var nodeset = {};
					nodeset.id = $scope.domianTopology.subdomainNodeIndex;
					$scope.domianTopology.subdomainNodeIndex++;
					nodeset.nodes = arr;
					nodeset.name = 'subdomain ' + subdomain;
					nodeset.x = parseInt(Math.random()*100);
					nodeset.y = parseInt(Math.random()*100);
					$scope.subdomaintopologyData.nodeSet.push(nodeset);
				}
			}
		}

		if(flag == 'link'){
			$scope.domianTopology.processSubdomainLinkNum++;
			if(data.length !== 0){
				if(data.hasOwnProperty('subdomain-link')){
					for(var linkIndex = 0; linkIndex < data['subdomain-link'].length; linkIndex++) {
						//console.log('process link', $scope.domianTopology.subdomainLinkIndex);
						var currentLink =  data['subdomain-link'][linkIndex];
						var srcId = $scope.subdomaintopologyData.nodesDict.getItem(data['subdomain-link'][linkIndex]['link-source']['source-node']);
						var srcTp = currentLink['link-source']['source-tp'];
						var tarId = $scope.subdomaintopologyData.nodesDict.getItem(data['subdomain-link'][linkIndex]['link-dest']['dest-node']);
						var tarTp = currentLink['link-dest']['dest-tp'];

						var linkContainer = {};
						var linkContainerIndex = null;
						var linkInfo;
						var showallLinks = false ;    //true-display all links; false-display same links in one link

						if(!showallLinks ){
							var currentLinkKey = getKey(srcId,tarId);
							if($scope.subdomaintopologyData.linksDict.contains(currentLinkKey)){
								linkContainerIndex = $scope.subdomaintopologyData.linksDict.getItem(getKey(srcId,tarId));
							}
							else {
								linkContainerIndex = $scope.subdomaintopologyData.links.length;
								$scope.subdomaintopologyData.linksDict.setItem(getKey(srcId,tarId), linkContainerIndex);
								$scope.subdomaintopologyData.links.push({
									id: linkContainerIndex,
									source: Math.min(srcId, tarId),
									target: Math.max(srcId, tarId),
									sourceTp: srcTp,
									targetTp: tarTp,
									links: []
								});
							}
						}else{
							linkContainerIndex = $scope.subdomaintopologyData.links.length;
							$scope.subdomaintopologyData.links.push({
								id: linkContainerIndex,
								source: srcId,
								target: tarId,
								sourceTp: srcTp,
								targetTp: tarTp,
								links: []
							});
						}

						linkContainer = $scope.subdomaintopologyData.links[linkContainerIndex];

						linkInfo = {
							// Internal ID
							id: $scope.domianTopology.subdomainLinkIndex,
							// Global ID
							linkId: currentLink['link-id'],
							// Source node ID
							source: $scope.subdomaintopologyData.nodesDict.getItem(currentLink['link-source']['source-node']),
							// Target node ID
							target: $scope.subdomaintopologyData.nodesDict.getItem(currentLink['link-dest']['dest-node']),
							// Source TP name
							sourceTP: currentLink['link-source']['source-tp'],
							// Target TP name
							targetTP: currentLink['link-dest']['dest-tp']
						};

						linkContainer.links.push(linkInfo);
						$scope.domianTopology.subdomainLinkIndex++;
					}
				}
			}
		}

		if(($scope.domianTopology.processSubdomainLinkNum == $scope.domianTopology.subdomainTopoLength) && ($scope.domianTopology.processSubdomainNodeNum == $scope.domianTopology.subdomainTopoLength)){
			//console.log('$scope.domianTopology.processSubdomainLinkNum == $scope.domianTopology.subdomainTopoLength');
			//console.log('$scope.subdomaintopologyData',$scope.subdomaintopologyData);
			if($scope.subdomaintopologyData.nodes.length === 0){
				$scope.displayAlert({
					title: "No nodes",
					text: "domain has no nodes",
					type: "error",
					confirmButtonText: "Close"
				});
			}
			else{
				$scope.showTopologyData = $scope.subdomaintopologyData;
				$scope.appConfig.currentDomain = $scope.domianTopology.domain;
			}
			$scope.domianTopology.subdomainTopoLength = 0;
			$scope.domianTopology.processSubdomainNodeNum = 0;
			$scope.domianTopology.processSubdomainLinkNum = 0;
			$scope.domianTopology.subdomainNodeIndex = 0;
			$scope.domianTopology.subdomainLinkIndex = 0;
			$scope.clearSubdomaintopologyData();
		}
	};

	$scope.LoadTeDomainTopology = function(domain){
		var hasSubdomain = true;
		$scope.teTopology.teDomain = domain;
		for(var i = 0; i < $scope.domainData.length; i++){
			if($scope.teTopology.teDomain === $scope.domainData[i]['domain-id']){
				$scope.teTopology.teSubdomain = $scope.domainData[i].subdomain;
				if($scope.teTopology.teSubdomain.length === 0){
					$scope.displayAlert({
						title: "No Subdomain",
						text: "domain " + domain + " has no subdomain",
						type: "error",
						confirmButtonText: "Close"
					});
					hasSubdomain = false;
				}
				break;
			}
		}
		if(hasSubdomain){
			$scope.teTopology.tesubdomainTopoLength = $scope.teTopology.teSubdomain.length;
			$scope.LoadTeSubdomainNode($scope.teTopology.teDomain , $scope.teTopology.teSubdomain[$scope.teTopology.teSubdomainCount]['sub-domain-id']);
		}
	};

	$scope.LoadTeSubdomainNode = function(domain, subdomain){
		BiermanRest.queryTeSubdomainNode(
			{
				'topology-id': $scope.appConfig.currentTopologyId,
				'domain-id': domain,
				'sub-domain-id': subdomain
			},
			function(data){
				$scope.processTeSubdomainTopology(subdomain, data, 'node');
				$scope.LoadTeSubdomainLink($scope.teTopology.teDomain, $scope.teTopology.teSubdomain[$scope.teTopology.teSubdomainCount]['sub-domain-id']);
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Subdomain nodes not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.LoadTeSubdomainLink = function(domain, subdomain){
		BiermanRest.queryTeSubdomainLink(
			{
				'topology-id': $scope.appConfig.currentTopologyId,
				'domain-id': domain,
				'sub-domain-id': subdomain
			},
			function(data){
				$scope.processTeSubdomainTopology(subdomain, data, 'link');
				$scope.teTopology.teSubdomainCount++;
				if($scope.teTopology.teSubdomainCount < $scope.teTopology.teSubdomain.length){
					$scope.LoadTeSubdomainNode($scope.teTopology.teDomain, $scope.teTopology.teSubdomain[$scope.teTopology.teSubdomainCount]['sub-domain-id']);
				}
				else{
					$scope.teTopology.teDomain = null;
					$scope.teTopology.teSubdomain = null;
					$scope.teTopology.teSubdomainCount = 0;
				}
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Subdomain links not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.processTeSubdomainTopology = function(subdomain, data, flag){
		//console.log('process Subdomain Topology', data);
		function getKey(a, b){
			if(a < b)
				return a + '-' + b;
			else
				return b + '-' + a;
		}

		if(flag == 'node'){
			$scope.teTopology.processteSubdomainNodeNum++;
			if(data.length !== 0){
				if(data.hasOwnProperty('te-subdomain-node')) {
					var arr = [];
					for(var nodeIndexBase = 0; nodeIndexBase < data['te-subdomain-node'].length; nodeIndexBase++){
						var currentNode = data['te-subdomain-node'][nodeIndexBase];
						var node = {};
						// Internal ID
						node.id = $scope.teTopology.teSubdomainNodeIndex;
						arr.push(node.id);
						$scope.teTopology.teSubdomainNodeIndex++;
						// Global ID
						node['node-id'] = currentNode['node-id'];
						node.name = currentNode['node-id'];
						// BFR local id
						//node.bfrLocalId = currentNode['topology-bier:bfr-local-id'];
						// Router ID
						//node.routerId = currentNode['topology-bier:router-id'];
						// Assign node's external id to the internal one
						$scope.subdomaintopologyData.nodesDict.setItem(node['node-id'], node.id);
						// Record node data
						$scope.subdomaintopologyData.nodes.push(node);

					}
					var nodeset = {};
					nodeset.id = $scope.teTopology.teSubdomainNodeIndex;
					$scope.teTopology.teSubdomainNodeIndex++;
					nodeset.nodes = arr;
					nodeset.name = 'subdomain ' + subdomain;
					nodeset.x = parseInt(Math.random()*100);
					nodeset.y = parseInt(Math.random()*100);
					$scope.subdomaintopologyData.nodeSet.push(nodeset);
				}
			}
		}

		if(flag == 'link'){
			$scope.teTopology.processteSubdomainLinkNum++;
			if(data.length !== 0){
				if(data.hasOwnProperty('te-subdomain-link')){
					for(var linkIndex = 0; linkIndex < data['te-subdomain-link'].length; linkIndex++) {
						//console.log('process link', $scope.teTopology.teSubdomainLinkIndex);
						var currentLink =  data['te-subdomain-link'][linkIndex];
						var srcId = $scope.subdomaintopologyData.nodesDict.getItem(data['te-subdomain-link'][linkIndex]['link-source']['source-node']);
						var srcTp = currentLink['link-source']['source-tp'];
						var tarId = $scope.subdomaintopologyData.nodesDict.getItem(data['te-subdomain-link'][linkIndex]['link-dest']['dest-node']);
						var tarTp = currentLink['link-dest']['dest-tp'];
						var linkContainer = {};
						var linkContainerIndex = null;
						var linkInfo;
						var showallLinks = false ;    //true-display all links; false-display same links in one link

						if(!showallLinks ){
							var currentLinkKey = getKey(srcId,tarId);
							if($scope.subdomaintopologyData.linksDict.contains(currentLinkKey)){
								linkContainerIndex = $scope.subdomaintopologyData.linksDict.getItem(getKey(srcId,tarId));
							}
							else {
								linkContainerIndex = $scope.subdomaintopologyData.links.length;
								$scope.subdomaintopologyData.linksDict.setItem(getKey(srcId,tarId), linkContainerIndex);
								$scope.subdomaintopologyData.links.push({
									id: linkContainerIndex,
									source: Math.min(srcId, tarId),
									target: Math.max(srcId, tarId),
									sourceTp: srcTp,
									targetTp: tarTp,
									links: []
								});
							}
						}else{
							linkContainerIndex = $scope.subdomaintopologyData.links.length;
							$scope.subdomaintopologyData.links.push({
								id: linkContainerIndex,
								source: srcId,
								target: tarId,
								sourceTp: srcTp,
								targetTp: tarTp,
								links: []
							});
						}

						linkContainer = $scope.subdomaintopologyData.links[linkContainerIndex];

						linkInfo = {
							// Internal ID
							id: $scope.teTopology.teSubdomainLinkIndex,
							// Global ID
							linkId: currentLink['link-id'],
							// Source node ID
							source: $scope.subdomaintopologyData.nodesDict.getItem(currentLink['link-source']['source-node']),
							// Target node ID
							target: $scope.subdomaintopologyData.nodesDict.getItem(currentLink['link-dest']['dest-node']),
							// Source TP name
							sourceTP: currentLink['link-source']['source-tp'],
							// Target TP name
							targetTP: currentLink['link-dest']['dest-tp']
						};

						linkContainer.links.push(linkInfo);
						$scope.teTopology.teSubdomainLinkIndex++;
					}
				}
			}
		}

		if(($scope.teTopology.processteSubdomainLinkNum == $scope.teTopology.tesubdomainTopoLength) && ($scope.teTopology.processteSubdomainNodeNum == $scope.teTopology.tesubdomainTopoLength)){
			//console.log('$scope.teTopology.processteSubdomainLinkNum == $scope.teTopology.tesubdomainTopoLength');
			//console.log('$scope.subdomaintopologyData',$scope.subdomaintopologyData);
			if($scope.subdomaintopologyData.nodes.length === 0){
				$scope.displayAlert({
					title: "No nodes",
					text: "domain has no nodes",
					type: "error",
					confirmButtonText: "Close"
				});
			}
			else{
				$scope.showTopologyData = $scope.subdomaintopologyData;
				$scope.appConfig.currentDomain = $scope.teTopology.teDomain;
			}
			$scope.teTopology.tesubdomainTopoLength = 0;
			$scope.teTopology.processteSubdomainNodeNum = 0;
			$scope.teTopology.processteSubdomainLinkNum = 0;
			$scope.teTopology.teSubdomainNodeIndex = 0;
			$scope.teTopology.teSubdomainLinkIndex = 0;
			$scope.clearSubdomaintopologyData();
		}
	};

	$scope.LoadAllTeDomainNode = function (){
		$scope.nodeForDeployChannel = [];
		$scope.mergeTeSubDomainNodeData = [];
		$scope.queryTeSubdomainNum = 0;
		$scope.returnTeSubdomainNum = 0;
		for(var domainLoop = 0; domainLoop < $scope.domainData.length; domainLoop++){

			for(var subdomainLoop = 0; subdomainLoop < $scope.domainData[domainLoop].subdomain.length; subdomainLoop++){
				$scope.queryTeSubdomainNum++;
				$scope.LoadAllTeSubDomainNode($scope.domainData[domainLoop]['domain-id'],
					$scope.domainData[domainLoop].subdomain[subdomainLoop]['sub-domain-id']);
			}
		}
	};

	$scope.LoadAllTeSubDomainNode = function(domain, subdomain){
		BiermanRest.queryTeSubdomainNode(
			{
				'topology-id': $scope.appConfig.currentTopologyId,
				'domain-id': domain,
				'sub-domain-id': subdomain
			},
			function(data){
				$scope.returnTeSubdomainNum++;
				console.log('$scope.returnTeSubdomainNum', $scope.returnTeSubdomainNum);
				if(data.hasOwnProperty('te-subdomain-node')){
					console.log('has te subdomain node',data);
					var subdomainData = $scope.processTeSubDomainNode(domain, subdomain, data['te-subdomain-node']);
					$scope.mergeTeSubDomainNodeData.push(subdomainData);
				}
				if($scope.returnTeSubdomainNum == $scope.queryTeSubdomainNum){
					$scope.mergeTeSubDomainNode();
				}
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Te Subdomain nodes not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.mergeTeSubDomainNode = function(){

		for(var domainLoop = 0; domainLoop < $scope.domainData.length; domainLoop++){
			var subDomain = [];
			for(var subdomainLoop = 0; subdomainLoop < $scope.domainData[domainLoop].subdomain.length; subdomainLoop++){
				for(var i = 0; i < $scope.mergeTeSubDomainNodeData.length; i++){
					if($scope.mergeTeSubDomainNodeData[i]['domain-id'] == $scope.domainData[domainLoop]['domain-id'] &&
						$scope.mergeTeSubDomainNodeData[i]['sub-domain-id'] ==
						$scope.domainData[domainLoop].subdomain[subdomainLoop]['sub-domain-id']){
						subDomain.push($scope.mergeTeSubDomainNodeData[i]);
						console.log('push   subDomain',subDomain);

					}
				}
			}
			var nodeDomain = {};
			nodeDomain['domain-id'] = $scope.domainData[domainLoop]['domain-id'];
			nodeDomain['sub-domain'] = subDomain;
			$scope.nodeForDeployChannel.push(nodeDomain);
		}
		console.log('$scope.nodeForDeployChannel',$scope.nodeForDeployChannel);
	};

	$scope.processTeSubDomainNode = function(domain, subdomain, biernode){
		var node = [];
		var nodeData = {};
		var nodeSize = biernode.length;
		var flag = false;
		if(nodeSize > 0){
			for(var nodeLoop = 0; nodeLoop < nodeSize; nodeLoop++){
				var nodeID = biernode[nodeLoop]['node-id'];
				var teDomain = biernode[nodeLoop]['bier-te-node-params']['te-domain'];
				for(var doaminLoop = 0; doaminLoop < teDomain.length; doaminLoop++){
					if(domain == teDomain[doaminLoop]['domain-id']){
						var teSubDomain = teDomain[doaminLoop]['te-sub-domain'];
						for(var subdomainLoop = 0; subdomainLoop < teSubDomain.length;
							subdomainLoop++){
							if(subdomain == teSubDomain[subdomainLoop]['sub-domain-id']){
								var teBsl = teSubDomain[subdomainLoop]['te-bsl'];
								nodeData = $scope.processTpIdInBSl(teBsl, nodeID);
								flag = true;
								break;
							}
						}
						break;
					}
				}
				if(flag){
					node.push(nodeData);
					nodeData = {};
					flag = false;
				}
				console.log('push node', node);
			}
			var subdomainData = {};
			subdomainData['sub-domain-id'] = subdomain;
			subdomainData.node = node;
			subdomainData['domain-id'] = domain;
		}
		return subdomainData;
	};

	$scope.processTpIdInBSl = function (bsl,nodeId){
		var tpIdExist = [];   //check tp-id duplication
		var tp = [];
		for(var bslLoop = 0; bslLoop < bsl.length; bslLoop++){
			var teSi = bsl[bslLoop]['te-si'];
			for(var siLoop = 0; siLoop < teSi.length; siLoop++){
				var teBp = teSi[siLoop]['te-bp'];
				for(var bpLoop = 0; bpLoop < teBp.length; bpLoop++){
					var tpId = teBp[bpLoop]['tp-id'];
					if(-1 == ($.inArray(tpId, tpIdExist))){
						tpIdExist.push(tpId);
						var tpData = {};
						tpData.tp = tpId;
						tp.push(tpData);
					}
				}
			}
		}
		var nodeData = {};
		nodeData['node-id'] = nodeId;
		nodeData.tp = tp;
		return nodeData;
	};

	$scope.getChannels = function(){
		$scope.channelData = [];
		BiermanRest.getChannel(
			$scope.appConfig.currentTopologyId,
			function(data){
				$scope.channelName = data;
				for(var i = 0; i < $scope.channelName.length; i++){
					$scope.queryChannel($scope.channelName[i].name);
				}
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Channels not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.queryChannel = function(channelname){
		var channel = [channelname];
		BiermanRest.queryChannel(
			{
				'topology-id': $scope.appConfig.currentTopologyId,
				'channel-name': channel
			},
			function(data){
				$scope.processChannelData(data[0]);
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Channels not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.processChannelData = function(channel){
		$scope.channelData.push(channel);
		console.log('processed channelData---', $scope.channelData);
	};

	$scope.queryDomain = function(){
		$scope.clearCurrentDomainData();

		BiermanRest.queryDomain(
			$scope.appConfig.currentTopologyId,
			function(data){
				$scope.domainData = [];
				$scope.domainDataLength = data.length;
				for(var i= 0; i< $scope.domainDataLength; i++){
					$scope.querySubdomain(data[i]['domain-id']);
					//console.log('domainid', data[i]['domain-id']);
				}
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "domain not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.querySubdomain = function(domain){
		BiermanRest.querySubdomain(
			{
				'topology-id': $scope.appConfig.currentTopologyId,
				'domain-id': domain
			},
			function(data){
				$scope.subdomainData = data;
				$scope.subdomainDataLength = data.length;
				for(var i = 0; i < data.length; i++){
					data[i].name = data[i]['sub-domain-id'];
				}
				$scope.processSubdomainData(domain, data);
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Subdomain not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.processSubdomainData = function(domain, data){
		var domaindata = {
			'name':'',
			'domain-id': '',
			'subdomain': ''
		};
		domaindata['domain-id'] = domain;
		domaindata.name = domain;
		domaindata.subdomain = data;
		$scope.domainData.push(domaindata);
		console.log('processed $scope.domainData---', $scope.domainData);
	};

	$scope.queryTopology = function(topoid){
		$scope.appConfig.queryTopology = 'yes';
		BiermanRest.queryTopology(
			topoid,
			function(data){
				$scope.appConfig.currentTopologyId = data['topology-id'];
				$scope.appConfig.currentDomain = null;
				$scope.nodeid = [];
				$scope.linkid = [];
				$scope.getChannels();
				$scope.queryDomain();
				if(data.hasOwnProperty('node-id')){
					$scope.nodelength = data['node-id'].length;
					$scope.topology = data;
					for(var i = 0; i < $scope.nodelength; i++){
						$scope.nodeid.push($scope.topology['node-id'][i]['node-id']);
					}
					$scope.queryNode($scope.nodeid);
					$scope.topoInitialized = true;
					if(data.hasOwnProperty('link-id')){
						$scope.linklength = data['link-id'].length;
						for(var j = 0; j < $scope.linklength; j++){
							$scope.linkid.push($scope.topology['link-id'][j]['link-id']);
						}
					}
				}
				else{
					$scope.appConfig.queryTopology = 'no';
					$scope.clearShowTopologyData();

					$scope.topologyData = $scope.showTopologyData;
					$scope.getNetconf();
				}
			},
			function(err){
				$scope.appConfig.queryTopology = 'no';
				console.error(err);
				$scope.displayAlert({
					title: "Topology not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};
	//query node
	$scope.queryNode = function(nodeId){
		BiermanRest.queryNode(
			{
				'topo-id': $scope.appConfig.currentTopologyId,
				'node': nodeId
			},
			function(data){
				console.log('query node data from ODL:', data);
				$scope.clearQueryTopology();
				$scope.topoDate.node = data.node;
				if($scope.linkid.length > 0)
					$scope.queryLink($scope.linkid);
				else{
					$scope.processTopologyData($scope.topoDate);
				}
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Node of Topology not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};
	//query link
	$scope.queryLink = function(linkid){
		//var link = [linkId];
		BiermanRest.queryLink(
			{
				'topo-id': $scope.appConfig.currentTopologyId,
				'link': linkid
			},
			function(data){
				console.log('query link data from ODL:', data);
				$scope.topoDate.link = data.link;
				$scope.processTopologyData($scope.topoDate);
			},
			function(err){
				console.error(err);
				$scope.displayAlert({
					title: "Link of Topology not loaded",
					text: err.errMsg,
					type: "error",
					confirmButtonText: "Close"
				});
			}
		);
	};

	$scope.processTopologyData = function(data){
		console.log('process Topology data', data);
		function getKey(a, b){
			if(a < b)
				return a + '-' + b;
			else
				return b + '-' + a;
		}

		if(data.hasOwnProperty('node')){
			for(var nodeIndex = 0; nodeIndex < data.node.length; nodeIndex++){
				var currentNode = data.node[nodeIndex];
				//console.log('process node', $scope.nodeIndex);
				var node = {};
				// Internal ID
				node.id = nodeIndex;
				// Global ID
				node['node-id'] = currentNode['node-id'];
				node.name = currentNode['node-id'];
				node.tp = currentNode['bier-termination-point'];
				// BFR local id
				//node.bfrLocalId = currentNode['topology-bier:bfr-local-id'];
				// Router ID
				//node.routerId = currentNode['topology-bier:router-id'];
				// Termination points information
				//node.tp = currentNode['bier-termination-point'][0]['tp-id'];
				// Assign node's external id to the internal one
				$scope.topologyMergerData.nodesDict.setItem(node['node-id'], node.id);
				// Record node data
				$scope.topologyMergerData.nodes.push(node);

			}
		}

		if(data.hasOwnProperty('link')){
			for(var linkIndex = 0; linkIndex < data.link.length; linkIndex++){
				//console.log('process link', $scope.linkIndex);
				var currentLink = data.link[linkIndex];
				var srcId = $scope.topologyMergerData.nodesDict.getItem(data.link[linkIndex]['link-source']['source-node']);
				var srcTp = currentLink['link-source']['source-tp'];
				var srcNodeId = currentLink['link-source']['source-node'];
				var tarId = $scope.topologyMergerData.nodesDict.getItem(data.link[linkIndex]['link-dest']['dest-node']);
				var tarTp = currentLink['link-dest']['dest-tp'];
				var tarNodeId = currentLink['link-dest']['dest-node'];

				var linkContainer = {};
				var linkContainerIndex = null;
				var linkInfo;
				var showallLinks = false ;

				if(!showallLinks ){
					var currentLinkKey = getKey(srcId,tarId);
					if($scope.topologyMergerData.linksDict.contains(currentLinkKey)){
						linkContainerIndex = $scope.topologyMergerData.linksDict.getItem(getKey(srcId,tarId));
					}
					else {
						linkContainerIndex = $scope.topologyMergerData.links.length;
						$scope.topologyMergerData.linksDict.setItem(getKey(srcId,tarId), linkContainerIndex);
						$scope.topologyMergerData.links.push({
							id: linkContainerIndex,
							source: Math.min(srcId, tarId),
							target: Math.max(srcId, tarId),
							sourceNode: srcNodeId,
							targetNode: tarNodeId,
							sourceTp: srcTp,
							targetTp: tarTp,
							links: []
						});
					}
				}
				else{
					linkContainerIndex = $scope.topologyMergerData.links.length;
					$scope.topologyMergerData.links.push({
						id: linkContainerIndex,
						source: srcId,
						target: tarId,
						sourceTp: srcTp,
						targetTp: tarTp,
						links: []
					});
				}

				linkContainer = $scope.topologyMergerData.links[linkContainerIndex];

				linkInfo = {
					// Internal ID
					id: linkIndex,
					// Global ID
					//linkId: currentLink['link-id'],
					linkId: currentLink['link-id'],
					// Source node ID
					source: $scope.topologyMergerData.nodesDict.getItem(currentLink['link-source']['source-node']),
					sourceTP: currentLink['link-source']['source-tp'],
					sourceNode: srcNodeId,
					// Target node ID
					target: $scope.topologyMergerData.nodesDict.getItem(currentLink['link-dest']['dest-node']),
					// Source TP name

					// Target TP name
					targetTP: currentLink['link-dest']['dest-tp'],
					targetNode: tarNodeId,
					// BFR adjustment ID
					//bfrAdjId: currentLink['topology-bier:bfr-adj-id'],
					// Delay of a link
					delay: currentLink.delay,
					// Loss info
					loss: currentLink.loss
				};
				//console.log('linkInfo-------------', linkInfo);
				linkContainer.links.push(linkInfo);
			}
		}
		$scope.topologyData = $scope.topologyMergerData;
		$scope.showTopologyData = $scope.topologyMergerData;   //for nextUI show
		$scope.clearCurrentTopologyMergerData();
		$scope.getNetconf();
		$scope.getBierTeLabelRange();
		$scope.appConfig.queryTopology = 'no';
		$scope.topologyData.nodes.sort($scope.compare("node-id"));
		console.log('$scope.topologyData..........', $scope.topologyData);
		return $scope.topologyData;
	};

	$scope.clearTopology = function(){
		//console.log('clear tree--------');
		$scope.clearCurrentTree();
		$scope.resetTopology();
		$scope.appConfig.mode = 'start';
	};

	$scope.initApp = function(){
		$scope.registerNotificationId();
		$scope.getBGPConfig();
		setTimeout(function(){
			$scope.getBierBGPConfig();}, 1000);
		console.log("init app");

	};
	$scope.initApp();

	$scope.openRightPanel = function(panelCode){
		$scope.appConfig.currentPanel = panelCode;
		$mdSidenav('right').open();
	};
	//open Channel Manager
	$scope.openChannelManager = function() {
		$scope.customFullscreen = $mdMedia('xs') || $mdMedia('sm');
		var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'))  && $scope.customFullscreen;
		$mdDialog.show({
			controller: function($scope, $mdDialog, dScope){

				$scope.edit = {
					name: '',
					editing: false
				};
				$scope.input = {
					'addChannel': {},
					'addChannelStatus': 'none',
					'editChannel': {},
					'editChannelStatus': 'none',
					'deployChannel':{},
					'deployChannelStatus': 'none',
					'deployTeChannel':{},
					'deployTeChannelStatus': 'none',
					'deployChannelType': null
				};

				// Hide dialog (close without discarding changes)
				$scope.hide = function() {
					$mdDialog.hide();
				};
				// Cancel (discard changes)
				$scope.cancel = function() {
					$mdDialog.cancel();
				};
				$scope.typeOf = function(val){
					return typeof val;
				};
				$scope.editChannel = function(val){
					$scope.edit.editing = true;
					$scope.edit.name = val;
				};
				$scope.closeEditor = function(val){
					$scope.edit.editing = false;
					$scope.edit.name = val;
				};

				$scope.chooseNodeData = [];
				$scope.chooseTpIdData = [];
				$scope.items = [null];
				var i = 1;
				//add input button dynamic
				$scope.Channel= {
					add: function () {
						$scope.items[i] = null;
						i++;
					},
					del: function (key) {
						//console.log(key);
						$scope.items.splice(key, 1);
						$scope.chooseTpIdData[key] = null;
						i--;
					}
				};
				$scope.display = function(){
					console.log('$scope.items', $scope.items);
				};

				$scope.removeChannel = function(chName){
					BiermanRest.removeChannel(
						{
							topologyId: dScope.appConfig.currentTopologyId,
							channelName: chName
						},
						function(){
							dScope.getChannels();
							dScope.displayAlert({
								title: "Channel Removed",
								text: "The channel " + chName + " has been removed",
								type: "success",
								timer: 1500,
								confirmButtonText: "Okay"
							});
						},
						function(err){
							console.error(err);
							dScope.displayAlert({
								title: "Channel Not Removed",
								text: err.errMsg,
								type: "error",
								confirmButtonText: "Close"
							});
						}
					);
				};

				$scope.addChannel = function(){
					$scope.input.addChannelStatus = 'inprogress';
					if(biermanTools.hasOwnProperties($scope.input.addChannel, ['name', 'srcIP', 'destGroup', 'sourceWildcard', 'groupWildcard', 'domain', 'subdomain'])){
						var channelName = $scope.input.addChannel.name;
						BiermanRest.addChannel(
							{
								'topology-id': dScope.appConfig.currentTopologyId,
								'channel-name': channelName,
								'src-ip': $scope.input.addChannel.srcIP,
								'dst-group': $scope.input.addChannel.destGroup,
								'source-wildcard': $scope.input.addChannel.sourceWildcard,
								'group-wildcard': $scope.input.addChannel.groupWildcard,
								'domain-id': $scope.input.addChannel.domain['domain-id'],
								'sub-domain-id': $scope.input.addChannel.subdomain['sub-domain-id']
							},
							// success
							function(data){
								$scope.input.addChannel = {};
								$scope.input.addChannelStatus = 'success';
								dScope.displayAlert({
									title: "Channel Added",
									text: "The channel " + channelName + " has been added to the system",
									type: "success",
									timer: 1500,
									confirmButtonText: "Okay"
								});
								dScope.getChannels();
							},
							// error
							function(err){
								console.error(err);
								dScope.displayAlert({
									title: "Channel Not Added",
									text: err.errMsg,
									type: "error",
									confirmButtonText: "Close"
								});
							}
						);
					}
					else{
						dScope.displayAlert({
							title: "Channel Not Added",
							text: "create a channel " + dScope.errMsg1,
							type: "error",
							confirmButtonText: "Close"
						});
					}
				};

				$scope.modifyChannel = function(name){
					$scope.input.editChannelStatus = 'inprogress';
					var send = true;
					if( $scope.input.editChannel.domain !== undefined){
						$scope.inputdomain = $scope.input.editChannel.domain['domain-id'];
						if( $scope.input.editChannel.subdomain !== undefined){
							var subdomain =  $scope.input.editChannel.subdomain['sub-domain-id'];
						}
						else{
							console.log('error');
							dScope.displayAlert({
								title: "Channel Not modified",
								text: "Domain and Subdomain must modify at the same time",
								type: "error",
								confirmButtonText: "Close"
							});
							send = false;
						}
					}
					else{
						if( $scope.input.editChannel.subdomain !== undefined){
							dScope.displayAlert({
								title: "Channel Not modified",
								text: "Domain and Subdomain must modify at the same time",
								type: "error",
								confirmButtonText: "Close"
							});
							send = false;
						}
					}
					if(send){
						var channelName = name;
						BiermanRest.editchannel(
							{
								'topology-id': dScope.appConfig.currentTopologyId,
								'channel-name': channelName,
								'src-ip': $scope.input.editChannel.srcIP,
								'dst-group': $scope.input.editChannel.destGroup,
								'source-wildcard': $scope.input.editChannel.sourceWildcard,
								'group-wildcard': $scope.input.editChannel.groupWildcard,
								'domain-id': $scope.inputdomain,
								'sub-domain-id': subdomain
							},
							// success
							function(data){
								$scope.input.editChannel = {};
								$scope.input.editChannelStatus = 'success';
								dScope.displayAlert({
									title: "Channel modified",
									text: "The channel " + channelName + " has been modified to the system",
									type: "success",
									timer: 1500,
									confirmButtonText: "Okay"
								});
								dScope.getChannels();
								$scope.closeEditor();
							},
							// error
							function(err){
								console.error(err);
								dScope.displayAlert({
									title: "Channel Not modified",
									text: err.errMsg,
									type: "error",
									confirmButtonText: "Close"
								});
							}
						);
					}
				};

				$scope.getPath = function(channel){
					BiermanRest.getPath(
						{
							'input':{
								'channel-name': channel
							}
						},
						function(link){
							var biLinkList = dScope.convertUniToBiLinks(link, true);
							dScope.highlightPath(biLinkList);
						},
						function(err){
							console.error(err);
							dScope.displayAlert({
								title: "Path not loaded",
								text: err.errMsg,
								type: "error",
								confirmButtonText: "Close"
							});
						}
					);
				};

				//deploy BIER channel
				$scope.deployChannel = function(){
					$scope.input.deployChannelStatus = 'inprogress';
					//console.log('deploy Channel---');
					dScope.processDeployChannelData(
						// success callback
						function(input){
							//console.log('input node', input);
							var nodes = input.input["egress-node"];
							var inode = input.input["ingress-node"];
							nodes.push({'node-id':inode});
							var num = 0;
							for(var i = 0; i < nodes.length; i++){
								for(var j = 0; j < dScope.netconfNode.length; j++){
									if(nodes[i]['node-id'] === dScope.netconfNode[j]['node-id']){
										if(dScope.netconfNode[j].ip !== null){
											num++;
											break;
										}
									}
								}
							}
							if(num != nodes.length){
								dScope.displayAlert({
									title: "Netconf  Not Configure",
									text: "You must add netconf for all " +  nodes.length + " nodes before deploy channel" ,
									type: "error",
									confirmButtonText: "Close"
								});
							}
							else if(biermanTools.hasOwnProperties($scope.input.deployChannel, ['name'])){
								input.input["egress-node"].pop();
								var channelName = $scope.input.deployChannel.name.name;
								input.input['channel-name']= channelName;
								input.input['bier-forwarding-type'] = 'bier';
								BiermanRest.deployChannel(input,
									// success callback
									function(response){
										$scope.input.deployChannelStatus = 'success';
										dScope.displayAlert({
											title: "Channel Deployed",
											text: "The channel " + channelName + " has been deployed to the system",
											type: "success",
											timer: 1500,
											confirmButtonText: "Okay"
										});
										dScope.clearTopology();
										dScope.getChannels();
										//console.log(response);
									},
									// error callback
									function(err){
										console.error(err);
										dScope.displayAlert({
											title: "Channel Deploy Failed",
											text: err.errMsg,
											type: "error",
											confirmButtonText: "Close"
										});
									}
								);
							}
							else{
								dScope.displayAlert({
									title: "Channel Not Deployed",
									text: "deploy a channel " + dScope.errMsg1,
									type: "error",
									confirmButtonText: "Close"
								});
							}
						},
						// error callback
						function(errMsg){
							console.error(errMsg);
							dScope.displayAlert({
								title: "Channel Deploy Failed",
								text: errMsg,
								type: "error",
								confirmButtonText: "Close"
							});
						}
					);
				};

				$scope.checkError = function () {
					dScope.displayAlert({
						title: "Channel Not Deployed",
						text: "deploy a channel " + dScope.errMsg1,
						type: "error",
						confirmButtonText: "Close"
					});
				};

				$scope.checkEgressNodes = function(){
					console.log('$scope.items',$scope.items);
					if($scope.items.length > 0){
						for(var i = 0; i < $scope.items.length; i++){
							if($scope.items[i] === null){
								$scope.checkError();
								return false;
							}
							else if($scope.items[i]['node-id'] === undefined ||
								$scope.items[i]['rcv-tp'] === undefined ){


								$scope.checkError();
								return false;
							}else if($scope.items[i]['rcv-tp'].length === 0){
								$scope.checkError();
								return false;
							}
						}
						return true;
					}
					else
						return false;
				};

				$scope.checkNetconf = function(){
					//console.log('checkNetconf');
					var nodes = $scope.items;
					var node = {};
					node['node-id'] = $scope.input.deployTeChannel.ingressNode;
					nodes.push(node);
					var nodeSize = nodes.length;
					var num = 0;
					for(var iLoop = 0; iLoop < nodes.length; iLoop++){
						for(var jLoop = 0; jLoop < dScope.netconfNode.length; jLoop++){
							if(nodes[iLoop]['node-id'] === dScope.netconfNode[jLoop]['node-id']){
								if(dScope.netconfNode[jLoop].ip !== null){
									num++;
									break;
								}
							}
						}
					}
					nodes.pop(node);
					if(num != nodeSize)
						return false;
					else
						return true;
				};

				//deploy BIER-TE channel
				$scope.deployTeChannel = function(){
					$scope.input.deployTeChannelStatus = 'inprogress';
					if(biermanTools.hasOwnProperties($scope.input.deployTeChannel,
							['name','ingressNode','ingressTpId']) && $scope.input.deployTeChannel.name !== null &&
						$scope.input.deployTeChannel.ingressTpId !== null && $scope.checkEgressNodes())
					{
						if($scope.checkNetconf()){
							BiermanRest.deployChannel(
								{
									"input":{
										"topology-id": dScope.appConfig.currentTopologyId,
										"channel-name": $scope.input.deployTeChannel.name.name,
										"ingress-node": $scope.input.deployTeChannel.ingressNode,
										"src-tp": $scope.input.deployTeChannel.ingressTpId.tp,
										"egress-node": $scope.items,
										"bier-forwarding-type":"bier-te"
									}
								},
								// success callback
								function(response){
									$scope.input.deployTeChannelStatus = 'success';
									dScope.displayAlert({
										title: "BIER-TE Channel Deployed",
										text: "The channel " + $scope.input.deployTeChannel.name.name + " has been deployed to the system",
										type: "success",
										timer: 1500,
										confirmButtonText: "Okay"
									});
									dScope.getChannels();
									$scope.chooseTpIdData = [];
									$scope.chooseNodeData = [];
									$scope.items = [null];
								},
								// error callback
								function(err){
									console.error(err);
									dScope.displayAlert({
										title: "Channel Deploy Failed",
										text: err.errMsg,
										type: "error",
										confirmButtonText: "Close"
									});
								}
							);
						}
						else{
							dScope.displayAlert({
								title: "Netconf  Not Configure",
								text: "You must add netconf for all nodes before deploy BIER-TE channel" ,
								type: "error",
								confirmButtonText: "Close"
							});
						}
					}
					else{
						$scope.checkError();
						/*
						dScope.displayAlert({
							title: "Channel Not Deployed",
							text: "deploy a channel " + dScope.errMsg1,
							type: "error",
							confirmButtonText: "Close"
						});*/
					}
				};

				$scope.channelChange = function(channel){
					console.log('choose channel', channel);
					//console.log('dScope.channelData', dScope.channelData);
					for(var iLoop = 0; iLoop < dScope.channelData.length; iLoop++){
						if(channel.name == dScope.channelData[iLoop].name){
							var domainId = dScope.channelData[iLoop]['domain-id'];
							var subDomainId = dScope.channelData[iLoop]['sub-domain-id'];
							$scope.chooseNode(domainId, subDomainId);
							break;
						}
					}
				};

				$scope.chooseNode = function(domainId, subDomainId){
					$scope.chooseNodeData = [];
					var flag = false;
					for(var domainLoop = 0; domainLoop < dScope.nodeForDeployChannel.length; domainLoop++){
						if(domainId == dScope.nodeForDeployChannel[domainLoop]['domain-id']){
							var subdomainData = dScope.nodeForDeployChannel[domainLoop]['sub-domain'];
							for(var subdomainLoop = 0; subdomainLoop < subdomainData.length; subdomainLoop++){
								if(subDomainId == subdomainData[subdomainLoop]['sub-domain-id']){
									$scope.chooseNodeData = subdomainData[subdomainLoop].node;

									flag = true;
									break;
								}
							}
							break;
						}
					}
					if(!flag){
						$scope.chooseNodeData = [];
					}
					console.log('$scope.chooseNodeData',$scope.chooseNodeData);
				};

				$scope.chooseTpId = function(nodeId,key) {
					var flag =false;
					for(var i = 0; i < $scope.chooseNodeData.length; i++){
						if(nodeId == $scope.chooseNodeData[i]['node-id']){
							$scope.chooseTpIdData[key] = $scope.chooseNodeData[i].tp;
							flag =true;
						}
					}
					if(!flag){
						$scope.chooseTpIdData[key] = null;
					}
				};

				$scope.chooseIngressTpId = function(nodeId) {
					var flag =false;
					for(var i = 0; i < $scope.chooseNodeData.length; i++){
						if(nodeId == $scope.chooseNodeData[i]['node-id']){
							$scope.chooseIngressTpIdData = $scope.chooseNodeData[i].tp;
							flag =true;
						}
					}
					if(!flag){
						$scope.chooseIngressTpIdData = null;
					}
				};

				$scope.clearTopology = function(){
					dScope.clearTopology();
				};

				$scope.dScope = dScope;
			},
			templateUrl: 'src/app/bierapp/src/templates/channel-manager.tpl.html',
			parent: angular.element(document.body),
			clickOutsideToClose: true,
			fullscreen: useFullScreen,
			locals: {
				dScope: $scope
			}
		})
			.then(function(answer) {
				$scope.status = 'You said the information was "' + answer + '".';
			}, function() {
				$scope.status = 'You cancelled the dialog.';
			});
		$scope.$watch(function() {
			return $mdMedia('xs') || $mdMedia('sm');
		}, function(wantsFullScreen) {
			$scope.customFullscreen = (wantsFullScreen === true);
		});
	};


	
  }]);
});
