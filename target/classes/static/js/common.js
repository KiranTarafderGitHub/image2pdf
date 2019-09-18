/*
 *This js should be included in all pages 
 *as it controls the common functionalities
 *throughout the project 
 */
$(document).ready(function(){
	$('#menu-button-left').click(function()
	{
		//alert("ckick");

	  	$('.navbar-sidenav').css('display','none');
	  	$('.content-wrapper, .container').css('margin-left','0px');
	  	$('#sidenavToggler').css('display','none');
	  	$(this).css('display','none');
	  	$('#menu-button-right').css('display','block');
	});

	$('#menu-button-right').click(function()
	{
		//alert("ckick");

	  	$('.navbar-sidenav').css('display','flex');
	  	$('.content-wrapper').css('margin-left','200px');
	  	$('#sidenavToggler').css('display','flex');
	  	$(this).css('display','none');
	  	$('#menu-button-left').css('display','block');
	});
	
	$('input.float').on('input', function() {
		  this.value = this.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');
	});
});

function bindJsEvents()
{
	$('input.float').on('input', function() {
		  this.value = this.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');
	});
}


function convert2MysqlDate(date)
{
    var todayTime = new Date(date);
    var month = todayTime .getMonth() + 1;
    var day = todayTime .getDate();
    var year = todayTime .getFullYear();

    //return month + "/" + day + "/" + year;
    return year+"-"+pad(month)+"-"+pad(day);
}

function pad(d)
{
	d = Number(d);
	return (d < 10) ? '0' + d.toString() : d.toString();
}