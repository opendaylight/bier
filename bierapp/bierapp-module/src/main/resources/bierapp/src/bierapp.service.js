define(['app/bierapp/src/bierapp.module'], function(bierapp) {


  bierapp.register.factory('BierRestangular', function(Restangular, ENV) {
    return Restangular.withConfig(function(RestangularConfig) {
      RestangularConfig.setBaseUrl(ENV.getBaseURL("MD_SAL"));
      //RestangularConfig.setDefaultHeaders({ "Content-Type": "application/json" }, { "Accept": "application/json" });
    });
  });

  bierapp.register.factory('BiermanRest', function(BierRestangular, $q){
    var s = {};

    s.registerNotificationId = function(input, successCbk, errorCbk) {
      var restObj = BierRestangular.one('restconf').one('operations').one('sal-remote:create-notification-stream');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) { 
          console.log('registerNotificationId data', data);
          successCbk(data.output['notification-stream-identifier']);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.websocketLocation = function(input, successCbk, errorCbk) {
      var restObj = BierRestangular.one('restconf').one('streams').one('stream').one(input);
      restObj.get().then(
        function(data) {
          console.log('websocketLocation data', data);
          successCbk(data.location);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.loadTopology = function(successCbk, errorCbk) {
      console.log('loadTopology  service');
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:load-topology');
      var reqData = {'input':{}};
      restObj.customPOST(reqData).then(
        function(data) {
          successCbk(data.output.topology);
          console.log('topology data', data);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.queryTopology = function(topologyId, successCbk, errorCbk) {
      console.log('queryTopology input', topologyId);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-topology');
      var reqData = {'input':{
                        'topology-id':topologyId
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('queryTopology data', data);
          successCbk(data.output);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.queryNode = function(input, successCbk, errorCbk) {
      console.log('query node input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-node');
      var reqData = {'input': {
                        'topology-id': input['topo-id'],
                        'node': input.node
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('queryNode data', data);
          successCbk(data.output);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.queryLink = function(input, successCbk, errorCbk) {
      console.log('query link input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-link');
      var reqData = {'input': {
                        'topology-id': input['topo-id'],
                        'link': input.link
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('querylink data', data);
          successCbk(data.output);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.queryDomain = function(topologyId, successCbk, errorCbk) {
      console.log('queryDomain input', topologyId);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-domain');
      var reqData = {'input':{
                        'topology-id': topologyId
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('queryDomain data', data);
          if (data.output.hasOwnProperty('domain')) {
            successCbk(data.output.domain);
          }
          else {
            successCbk([]);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.querySubdomain = function(input, successCbk, errorCbk) {
      console.log('querySubdomain input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-subdomain');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'domain-id': input['domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('querySubdomain data', data);
          if (data.output.hasOwnProperty('subdomain')) {
            successCbk(data.output.subdomain);
          }
          else {
            successCbk([]);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.configureDomain = function(input, successCbk, errorCbk) {
      console.log('configureDomain input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:configure-domain');
      var reqData = {'input':{
                        'topology-id':  input['topology-id'],
                        'domain':  input.domain
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('configureDomain data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.configureSubdomain = function(input, successCbk, errorCbk) {
      console.log('configureSubdomain input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:configure-subdomain');
      var reqData = {'input':{
                        'topology-id':  input['topology-id'],
                        'domain-id':  input['domain-id'],
                        'sub-domain':  input['sub-domain']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('configureSubdomain data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeDomain = function(input, successCbk, errorCbk) {
      console.log('removeDomain input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:delete-domain');
      var reqData = {'input':{
                        'topology-id':  input['topology-id'],
                        'domain-id': input['domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeDomain data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeSubdomain = function(input, successCbk, errorCbk) {
      console.log('removeSubdomain input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:delete-subdomain');
      var reqData = {'input':{
                        'topology-id':  input['topology-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeSubdomain data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.getChannel = function(topologyId, successCbk, errorCbk) {
      console.log('getChannel input', topologyId);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-channel-api:get-channel');
      var reqData = {'input':{
                        'topology-id':topologyId
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('getChannel data', data);
          if (data.output.hasOwnProperty('channel-name')) {
            successCbk(data.output['channel-name']);
          }
          else {
            successCbk([]);
          }
          
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.queryChannel = function(input, successCbk, errorCbk) {
      console.log('queryChannel input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-channel-api:query-channel');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'channel-name': input['channel-name']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('queryChannel data', data);
          successCbk(data.output.channel);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.addChannel = function(input, successCbk, errorCbk) {
      console.log('addChannel input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-channel-api:add-channel');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'name': input['channel-name'],
                        'src-ip': input['src-ip'],
                        'dst-group': input['dst-group'],
                        'source-wildcard': input['source-wildcard'],
                        'group-wildcard': input['group-wildcard'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('addChannel data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.editchannel = function(input, successCbk, errorCbk) {
      console.log('editchannel input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-channel-api:modify-channel');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'name': input['channel-name'],
                        'src-ip': input['src-ip'],
                        'dst-group': input['dst-group'],
                        'source-wildcard': input['source-wildcard'],
                        'group-wildcard': input['group-wildcard'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('editchannel data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeChannel = function(input, successCbk, errorCbk) {
      console.log('removeChannel input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-channel-api:remove-channel');
      var reqData = {'input':{
                        'topology-id': input.topologyId,
                        'channel-name': input.channelName
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeChannel data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.deployChannel = function(input, successCbk, errorCbk) {
      console.log('deployChannel input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-channel-api:deploy-channel');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('deployChannel data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.getPath = function(input, successCbk, errorCbk) {
      console.log('getPath input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-pce:query-bier-instance-path');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('getPath data', data);
          if (data.output.hasOwnProperty('link')) {
            successCbk(data.output.link);
          }
          else {
            errorCbk({'errMsg': 'No calculated Path'});
          }
          
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.getNetconf = function(successCbk, errorCbk) {
    
      var restObj = BierRestangular.one('restconf').one('operational').one('network-topology:network-topology').one('topology').one('topology-netconf');
      restObj.get().then(
        function(data) {     
          console.log('getNetconf data', data);
          successCbk(data.topology[0]);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.getNetconfConfig = function(successCbk, errorCbk,nodeId){
        var restObj = BierRestangular.one('restconf').one('config').one('network-topology:network-topology').one('topology').one('topology-netconf').one('node').one(nodeId);
        restObj.get().then(
      // loaded
          function (res){
            console.log("get netconf config restObj------", res);
            if(res.hasOwnProperty('node')){
              successCbk(res.node);
            } else {
              errorCbk({'errMsg': 'Node info is null'});
            }
          },
          // failed
          function(e){
            errorCbk({'errObj': e, 'errId': 0, 'errMsg': 'get netconf config failed'});
          });
    };

    s.addNodeNetconf = function(input, successCbk, errorCbk) {
      console.log('addNodeNetconf input', input);
      var restObj = BierRestangular.one('restconf').one('config').one('network-topology:network-topology').one('topology').one('topology-netconf').one('node').one(input.node['node-id']);
      var reqData = input;
      restObj.customPUT(reqData).then(
        function(data) {     
          console.log('addNodeNetconf data', data);
          successCbk();
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.editNodeNetconf = function(input, successCbk, errorCbk) {
      console.log('editNodeNetconf input', input);
      var restObj = BierRestangular.one('restconf').one('config').one('network-topology:network-topology').one('topology').one('topology-netconf').one('node').one(input.node['node-id']);
      var reqData = input;
      restObj.customPUT(reqData).then(
        function(data) {     
          console.log('editNodeNetconf data', data);
          successCbk();
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeNodeNetconf = function(input, successCbk, errorCbk) {
      console.log('removeNodeNetconf input', input);
      var restObj = BierRestangular.one('restconf').one('config').one('network-topology:network-topology').one('topology').one('topology-netconf').one('node').one(input);
      restObj.remove().then(
        function(data) {     
          console.log('removeNodeNetconf data', data);
          successCbk();
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.configNode = function(input, successCbk, errorCbk) {
      console.log('configNode input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-config-api:configure-node');
      var reqData = {'input':{
                        'topology-id':  input['topology-id'],
                        'node-id':  input['node-id'],
                        'domain':  input.domain
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('configNode data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeNodeFromSubdomain = function(input, successCbk, errorCbk) {
      console.log('removeNodeFromSubdomain input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-config-api:delete-node');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'node-id': input['node-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeNodeFromSubdomain data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeIpv4 = function(input, successCbk, errorCbk) {
      console.log('removeIpv4 input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-config-api:delete-ipv4');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'node-id': input['node-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id'],
                        'ipv4': input.ipv4
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeIpv4 data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.configTeNode = function(input, successCbk, errorCbk) {
      console.log('configTeNode input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-config-api:configure-te-node');
      var reqData = {'input':{
                        'topology-id':  input['topology-id'],
                        'node-id':  input['node-id'],
                        'te-domain':  input['te-domain']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('configTeNode data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeBSL = function(input, successCbk, errorCbk) {
      console.log('removeBSL input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-config-api:delete-te-bsl');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'node-id': input['node-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id'],
                        'bitstringlength': input.bitstringlength
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeBSL data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeSI = function(input, successCbk, errorCbk) {
      console.log('removeSI input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-config-api:delete-te-si');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'node-id': input['node-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id'],
                        'bitstringlength': input.bitstringlength,
                        'si': input.si
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeSI data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeBP = function(input, successCbk, errorCbk) {
      console.log('removeBP input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-config-api:delete-te-bp');
      var reqData = {'input':{
                        'topology-id': input['topology-id'],
                        'node-id': input['node-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id'],
                        'bitstringlength': input.bitstringlength,
                        'si': input.si,
                        'tp-id':input['tp-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeBP data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.configureBierTeLabelRange = function(input, successCbk, errorCbk) {
      console.log('configureBierTeLabelRange input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-config-api:configure-te-label');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('configureBierTeLabelRange data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeBierTeLabelRange = function(input, successCbk, errorCbk) {
      console.log('removeBierTeLabelRange input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-config-api:delete-te-label');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeBierTeLabelRange data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.querySubdomainNode = function(input, successCbk, errorCbk) {
      console.log('querySubdomainNode input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-subdomain-node');
      var reqData = {'input': {
                        'topology-id': input['topology-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('querySubdomainNode data', data);
          successCbk(data.output);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.querySubdomainLink = function(input, successCbk, errorCbk) {
      console.log('querySubdomainLink input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-subdomain-link');
      var reqData = {'input': {
                        'topology-id': input['topology-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('querySubdomainLink data', data);
          successCbk(data.output);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.queryTeSubdomainNode = function(input, successCbk, errorCbk) {
      console.log('queryTeSubdomainNode input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-te-subdomain-node');
      var reqData = {'input': {
                        'topology-id': input['topology-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('queryTeSubdomainNode data', data);
          successCbk(data.output);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.queryTeSubdomainLink = function(input, successCbk, errorCbk) {
      console.log('queryTeSubdomainLink input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-topology-api:query-te-subdomain-link');
      var reqData = {'input': {
                        'topology-id': input['topology-id'],
                        'domain-id': input['domain-id'],
                        'sub-domain-id': input['sub-domain-id']
                      }
                    };
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('queryTeSubdomainLink data', data);
          successCbk(data.output);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.startEchoReq = function(input, successCbk, errorCbk) {
      console.log('startEchoReq input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-oam-api:start-echo-request');
      var reqData = input;
      restObj.customPOST(reqData).then(
          function(data) {
              console.log('startEchoReq data', data);
              successCbk(data.output);
          },function(res) {
              if (res.data.hasOwnProperty('errors')) {
                  var errDetails = '';
                  for(var i = 0; i < res.data.errors.error.length; i++){
                      errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
                  }
                  errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
              }
              else {
                  errorCbk(res);
              }
          });
    };

    s.configBGP = function(input, successCbk, errorCbk) {
      console.log('configBGP input', input);
      var restObj = BierRestangular.one('restconf').one('config').one('openconfig-network-instance:network-instances');
      var reqData = input;
      restObj.customPUT(reqData).then(
        function(data) {     
          console.log('configBGP data', data);
          successCbk();
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.getBGPConfig = function(successCbk, errorCbk) {
      console.log('configBGP');
      var restObj = BierRestangular.one('restconf').one('config').one('openconfig-network-instance:network-instances');
      restObj.get().then(
        function(data) {     
          console.log('configBGP data', data);
          successCbk(data['network-instances']);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };


    s.configBierBGP = function(input, successCbk, errorCbk) {
      console.log('configBierBGP input', input);
      var restObj = BierRestangular.one('restconf').one('config').one('bier-bgp-config:config-bgp-info');
      var reqData = input;
      restObj.customPUT(reqData).then(
        function(data) {     
          console.log('configBierBGP data', data);
          successCbk();
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.getBierBGPConfig = function(successCbk, errorCbk) {
      console.log('getBierBGPConfig');
      var restObj = BierRestangular.one('restconf').one('config').one('bier-bgp-config:config-bgp-info');
      restObj.get().then(
        function(data) {     
          console.log('getBierBGPConfig data', data);
          successCbk(data['config-bgp-info']);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.configRecBSL = function(input, successCbk, errorCbk) {
      console.log('configureRecommendBSL input', input);
      var restObj = BierRestangular.one('restconf').one('config')
      .one('bier-bp-allocate-params-config:bier-bp-allocate-params').one('topo-bp-allocate-params')
      .one(input.topologyId).one('recommend-bsl');
      var reqData = input.data;
      restObj.customPUT(reqData).then(
        function(data) {     
          console.log('configureRecommendBSL data', data);
          successCbk();
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.getBpAllocateParams = function(successCbk, errorCbk) {
      console.log('getBpAllocateParams');
      var restObj = BierRestangular.one('restconf').one('config').one('bier-bp-allocate-params-config:bier-bp-allocate-params');
      restObj.get().then(
        function(data) {     
          console.log('getBpAllocateParams data', data);
          successCbk(data['bier-bp-allocate-params']);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.autoConfigTeNode = function(input, successCbk, errorCbk) {
      console.log('autoConfigTeNode input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-config-api:configure-te-subdomain');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('autoConfigTeNode data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.removeSD = function(input, successCbk, errorCbk) {
      console.log('removeSD input', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-config-api:delete-te-subdomain');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {     
          console.log('removeSD data', data);
          if (data.output['configure-result'].result == 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
              for(var i = 0; i < res.data.errors.error.length; i++){
                errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
              }
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.configureTeFrr = function(input, successCbk, errorCbk) {
      console.log('configureTeFrr input:', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-frr-config-api:configure-te-frr');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {
          console.log('configureTeFrr data:', data);
            if (data.output['configure-result'].result === 'FAILURE') {
              errorCbk({'errMsg': data.output['configure-result'].errorCause});
            }
            else {
              successCbk(data.output['configure-result'].result);
            }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
            for(var i = 0; i < res.data.errors.error.length; i++){
              errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
            }
            errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.deleteTeFrr = function(input, successCbk, errorCbk) {
      console.log('deleteTeFrr input:', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-frr-config-api:delete-te-frr');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {
          console.log('deleteTeFrr data', data);
          if (data.output['configure-result'].result === 'FAILURE') {
            errorCbk({'errMsg': data.output['configure-result'].errorCause});
          }
          else {
            successCbk(data.output['configure-result'].result);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
            for(var i = 0; i < res.data.errors.error.length; i++){
              errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
            }
            errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.queryLinkTeInfo = function(input, successCbk, errorCbk) {
      console.log('queryLinkTeInfo input:', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-te-frr-config-api:query-link-te-info');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {
          console.log('queryLinkTeInfo data:', data);
          if (data.output.hasOwnProperty('te-domain')) {
            successCbk(data.output['te-domain']);
          }
          else {
            successCbk([]);
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
            for(var i = 0; i < res.data.errors.error.length; i++){
              errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
            }
            errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.getLinkFrrData = function(successCbk, errorCbk) {
      console.log('getLinkFrrData');
      var restObj = BierRestangular.one('restconf').one('config').one('bier-frr:te-frr-configure');
      restObj.get().then(
        function(data) {
          console.log('getLinkFrrData data', data);
          successCbk(data['te-frr-configure']);
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
            var flag = false;
            for (var i = 0; i < res.data.errors.error.length; i++){
              if (res.data.errors.error[i]['error-message'] == "Request could not be completed because the relevant data model content does not exist ") {
                flag = true;
                break;
              }
              errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
            }
            if (flag) {
              successCbk('null');
            }
            else {
              errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
            }
          }
          else {
            errorCbk(res);
          }
        });
    };

    s.queryTeFrrPath = function(input, successCbk, errorCbk) {
      console.log('queryTeFrrPath input:', input);
      var restObj = BierRestangular.one('restconf').one('operations').one('bier-pce:query-te-frr-path');
      var reqData = input;
      restObj.customPOST(reqData).then(
        function(data) {
          console.log('queryTeFrrPath data:', data);
          if (data.output.hasOwnProperty('link')) {
            successCbk(data.output.link);
          }
          else {
            errorCbk({'errMsg': 'No calculated Path'});
          }
        },function(res) {
          if (res.data.hasOwnProperty('errors')) {
            var errDetails = '';
            for(var i = 0; i < res.data.errors.error.length; i++){
              errDetails = errDetails + '[' + i + '] ' + res.data.errors.error[i]['error-message'];
            }
            errorCbk({'errMsg': 'Controller found out errors: ' + errDetails});
          }
          else {
            errorCbk(res);
          }
        });
    };

    return s;
  });

});
