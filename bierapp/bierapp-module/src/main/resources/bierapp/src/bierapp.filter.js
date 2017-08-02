define(['app/bierapp/src/bierapp.module'], function(bierapp) {

  bierapp.filter('paging', function() {
    return function(lists,start){     //lists is origin data in ng-repeat, start value is currentPage*listsPerPage
			if(lists !== undefined)
			return lists.slice(start);
		};
  });

});