$(function() {
   			$('#tgl_query').bootstrapToggle({
   					on: 'Show Queries',
   					off: 'Hide Queries',
   					size: 'small',
					onstyle: 'success',
					offstyle: 'warning'
   				});
			$('#tgl_query').change(function() {
				$(".query_string").toggle();
		    });
})
