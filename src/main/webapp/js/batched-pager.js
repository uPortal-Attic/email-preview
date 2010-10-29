/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var unicon = unicon || {};

(function($, fluid){

    var computePageLimit = function(model) {
        return Math.min(model.totalRange, (model.pageIndex + 1)*model.pageSize);
    };

	/**
	 * Batched pager
	 */
	unicon.batchedpager = function(container, options) {
		
	    var that = fluid.initView("unicon.batchedpager", container, options);

	    /**
	     * Substitute for the default fluid direct model filter
	     */
	    var batchedModelFilter = function (model, pagerModel, perm) {
	        var limit = computePageLimit(pagerModel);
	        var start = pagerModel.pageIndex * pagerModel.pageSize;
	        // check to see if our current batch already include's this page
	        if (start < that.state.batchStart || limit > (that.state.batchStart + that.options.batchSize)) {
	            // if it doesn't, replace the model with the new batch
	            var m = new Array();
	            // if the page size has been changed to a value larger than our
	            // batch size, update the batch size to match
	            if (pagerModel.pageSize > that.options.batchSize) {
	                that.options.batchSize = pagerModel.pageSize;
	            }
	            that.state.batchStart = Math.floor(start / that.options.batchSize) * that.options.batchSize;
	            $(that.options.dataFunction(that.state.batchStart, that.options.batchSize, that.state.sortKey, that.state.sortDir)).each(function(){
	                m.push(this);
	            });
	            // rebind the model data
	            fluid.clear(that.pager.options.dataModel);
	            fluid.model.copyModel(that.pager.options.dataModel, m);
	            that.pager.events.onModelChange.fire(pagerModel, that.pager.model, that.pager);
	        }
	        // copied from the default implementation
	        var togo = [];
	        var end = Math.min(limit-that.state.batchStart, that.options.batchSize);
	        for (var i = pagerModel.pageIndex * pagerModel.pageSize - that.state.batchStart; i < end; ++ i) {
	            var index = perm? perm[i]: i;
	            togo[togo.length] = {index: index, row: model[index]};
	        }
	        return togo;
	    };

	    /**
	     * Substitute for the default fluid sorter
	     */
	    var batchedSorter = function (overallThat, model) {

	        // if the sorting hasn't been changed, just return
	        if (model.sortKey == that.state.sortKey && model.sortDir == that.state.sortDir)
	            return;
	
			// if the sort key has changed, assume we'd like it sorted in ascending order first
			if (model.sortKey != that.state.sortKey) {
				model.sortDir = 1;
			}
        
	        // update our current state with the new sorting preferences
	        that.state.sortKey = model.sortKey;
	        that.state.sortDir = model.sortDir;
	        that.state.batchStart = 0;
        
	        // if the total number of elements is less than the batch size, just
	        // use the default sorting behavior
	        if (model.totalRange <= that.options.batchSize) {
	            return fluid.pager.basicSorter(overallThat, model);
	        }
        
	        // otherwise, replace the pager's model with a new batch of data
	        var test = that.options.dataFunction(that.state.batchStart, that.options.batchSize, that.state.sortKey, that.state.sortDir);
	        fluid.clear(that.pager.options.dataModel);
	        fluid.model.copyModel(that.pager.options.dataModel, test);
	    };

	    that.refreshView = function() {
	    	var data = that.options.dataFunction(0, Math.min(that.options.batchSize, length));
	        // re-copy the list of items into the pager model and update
	        // the expected number of list items
	        fluid.clear(that.pager.options.dataModel);
	        fluid.model.copyModel(that.pager.options.dataModel, data);
	        var newModel = fluid.copy(that.pager.model);
	        newModel.totalRange = that.options.dataLengthFunction(data);
	        newModel.pageIndex = 0;
	        newModel.sortKey = that.state.sortKey;
	        newModel.sortDir = that.state.sortDir;
	        newModel.pageCount = Math.max(1, Math.floor((newModel.totalRange - 1)/ newModel.pageSize) + 1);
	        fluid.model.copyModel(that.pager.model, newModel);
	        that.pager.events.onModelChange.fire(newModel, that.pager.model, that.pager);
	    };
        
        // default options
		that.state = {
			batchStart: 0
		};
		var length = that.options.dataLengthFunction();
        var data = that.options.dataFunction(0, Math.min(that.options.batchSize, length));

		// modify the provided pager options to use the batched pager
		var pagerOptions = options.pagerOptions;
		pagerOptions.dataModel = data;
		pagerOptions.sorter = batchedSorter;
		pagerOptions.modelFilter = batchedModelFilter;
       
        // create the pager
        that.pager = fluid.pager(container, pagerOptions);
       
        // update the model's page range
        var newModel = fluid.copy(that.pager.model);
        newModel.totalRange = length;
        newModel.pageCount = Math.max(1, Math.floor((newModel.totalRange - 1)/ newModel.pageSize) + 1);
        newModel.sortKey = that.state.sortKey;
        newModel.sortDir = that.state.sortDir;
        that.pager.events.onModelChange.fire(newModel, that.pager.model, that.pager);
        fluid.model.copyModel(that.pager.model, newModel);

	    return that;
    
	};

	fluid.defaults("unicon.batchedpager", {
	    batchSize: 20,
	    dataFunction: null,
	    dataLengthFunction: null
	});

})(jQuery, fluid);