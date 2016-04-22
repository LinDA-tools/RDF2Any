$(function() {
   			$('#tgl_query').bootstrapToggle({
   					on: 'Hide Queries',
   					off: 'Show Queries',
   					size: 'small',
					onstyle: 'success',
					offstyle: 'warning'
   				});
			$('#tgl_query').change(function() {
				$(".query_string").toggle();
		    });
})
