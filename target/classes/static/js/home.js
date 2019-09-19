var startYear = 2015;
var endYear = 2050;
$(document).ready(function(){
	
	defaultSetting();
	
	$( "#dateRecieved" ).datepicker({
		dateFormat: "dd-mm-yy",
		
	});
	$('#tableAtmData').DataTable({
        responsive: true
    });
	
	for(var i = startYear;i <= endYear;i++)
	$('#selectYear').append($('<option>', {
	    value: i,
	    text: i
	}));
	
	for(var i = startYear;i <= endYear;i++)
	$('#SelectDataYear').append($('<option>', {
	    value: i,
	    text: i
	}));
	
	$('#suggestionList').focusout(function(){
		$('#searchSuggestionDiv').css('display','none');
	})
	
});



function defaultSetting()
{
	//searchSuggestionDiv
	$('#searchSuggestionDiv').css('display','none');
	$('#atmdetailRow').css('display','none');
}
function showUploadModal()
{
	//alert("hi");
	$('#modalUpload').modal('show');
}
function search()
{
	var searchItem = $('#searchBar').val();
	if(searchItem == ""){$('#searchSuggestionDiv').css('display','none');$('#atmdetailRow').css('display','none');return false;}
		
	
	$.ajax({
		type:"POST",
		url:"/search",
		data:{
			searchItem: searchItem
		},
		success:function(response)
		{
			try
			{
				var json = JSON.parse(response);
				if(json.status == "success")
				{showSearchResult(json);}
				else
				{showSearchResultWhenErrorOrEmpty();}
			}
			catch(e)
			{showSearchResultWhenErrorOrEmpty();}
		},
		error:function(response)
		{showSearchResultWhenErrorOrEmpty();},
		complete:function(response)
		{}
	});
}

function showSearchResult(json)
{
	//$('#branchDistrict').prop('disabled', false);
	//searchSuggestionDiv  //suggestionList
	$('#suggestionList').empty();
	$('#searchSuggestionDiv').css('display','block');
	var data = json.data;
	console.log(data);
	var atms = JSON.parse(data);
	var suggestionLength = atms.length; 
	if(suggestionLength > 0)
	{
		for( i = 0;i<suggestionLength;i++)
		{
			//console.log(ma[i].month+"    "+ma[i].amount);
			$('#suggestionList').append('<li atmid="'+atms[i].atmId+'" client="'+atms[i].client+'" address="'+atms[i].atmAddress+'" onclick="getAtmData('+atms[i].id+',this)">'+atms[i].atmId+', '+atms[i].bank+', '+atms[i].client+', '+atms[i].state+'</li>');
		}
	}
	else
	{showSearchResultWhenErrorOrEmpty();}
	
}

function showSearchResultWhenErrorOrEmpty()
{
	$('#suggestionList').empty();
	$('#searchSuggestionDiv').css('display','block');
	$('#suggestionList').append('<li>No Data Found</li>');

}
function getAtmDataForYear(elem)
{
	var atmMasterId = $('#atmMasterId').val();
	var year = $("#SelectDataYear").val();
	//alert(atmMasterId+" "+year);
	
	$.ajax({
		type:"POST",
		url:"/getAtmData",
		data:{
			atmMasterId: atmMasterId,
			year: year
		},
		success:function(response)
		{
			try
			{
				var json = JSON.parse(response);
				if(json.status == "success")
				{setAtmData(json);}
				else
				{setAtmDataWhenErrorOrEmpty();}
			}
			catch(e)
			{setAtmDataWhenErrorOrEmpty();}
		},
		error:function(response)
		{setAtmDataWhenErrorOrEmpty();},
		complete:function(response)
		{}
	});
}
function getAtmData(atmMasterId,elem)
{
	var currentTime = new Date();
	var year = currentTime.getFullYear();
	$('#SelectDataYear option[value="'+year+'"]').attr("selected", "selected");
	
	$('#atmMasterId').val(atmMasterId);
	$('#searchBar').val($(elem).attr("atmid"));
	
	//set the atm description details
	$('#atmid').text($(elem).attr("atmid").trim());
	$('#atmAddress').text($(elem).attr("address"));
	$('#atmClient').text($(elem).attr("client"));
	
	//set upload atm id
	$('#uploadAtmId').val($(elem).attr("atmid").trim());
	$('#searchSuggestionDiv').css('display','none');

	$.ajax({
		type:"POST",
		url:"/getAtmData",
		data:{
			atmMasterId: atmMasterId,
			year: year
		},
		success:function(response)
		{
			try
			{
				var json = JSON.parse(response);
				if(json.status == "success")
				{setAtmData(json);}
				else
				{setAtmDataWhenErrorOrEmpty();}
			}
			catch(e)
			{setAtmDataWhenErrorOrEmpty();}
		},
		error:function(response)
		{setAtmDataWhenErrorOrEmpty();},
		complete:function(response)
		{}
	});
}

function setAtmData(json)
{
	//$('#branchDistrict').prop('disabled', false);
	//searchSuggestionDiv  //suggestionList //$("#tableId > tbody").html(""); atmid atmAddress atmClient
	$('#tableAtmData tbody').empty();
	$('#atmdetailRow').css('display','block');
	var data = json.data;
	//console.log(data);
	
	var atmdata = JSON.parse(data);
	var atmdataLength = atmdata.length;
	if(atmdataLength > 0)
	{
		for( i = 0;i<atmdataLength;i++)
		{
			var csrFilePth = encodeURI(atmdata[i].csrLink);
			var atnFilePth =encodeURI(atmdata[i].atnLink);
			var bfeFilePth =encodeURI(atmdata[i].bfLink);
			
			
			var row = '<tr><td>'+convertDate(atmdata[i].dataDate)+'</td><td><span  onclick="download('+"'"+csrFilePth+"'"+')"  target="_blank"><i class="fas fa-lg fa-download"></i></span></td><td><span onclick="download('+"'"+atnFilePth+"'"+')" ><i class="fas fa-lg fa-download"></i></span></td><td><span onclick="download('+"'"+bfeFilePth+"'"+')"><i class="fas fa-lg fa-download"></i></span></td><td>'+convertToCustomISO(atmdata[i].uploadDate)+'</td></tr>';
			$('#tableAtmData').append(row);
		}
	}
	else
	{
		setAtmDataWhenErrorOrEmpty();
	}
	
}

function setAtmDataWhenErrorOrEmpty()
{
	var row = '<tr><td colspan="5" class="text-center"> No Data Found </td></tr>';
	$('#tableAtmData').append(row);
}

function convertDate(date)
{
	var dateParts = date.split('-');
	var year = dateParts[0];
	var monthInNumber = dateParts[1];
	var month = "";
	if(monthInNumber == '01' || monthInNumber == '1'){month = "January";}
	else if(monthInNumber == '02' || monthInNumber == '2'){month = "February";}
	else if(monthInNumber == '03' || monthInNumber == '3'){month = "March";}
	else if(monthInNumber == '04' || monthInNumber == '4'){month = "April";}
	else if(monthInNumber == '05' || monthInNumber == '5'){month = "May";}
	else if(monthInNumber == '06' || monthInNumber == '6'){month = "June";}
	else if(monthInNumber == '07' || monthInNumber == '7'){month = "July";}
	else if(monthInNumber == '08' || monthInNumber == '8'){month = "August";}
	else if(monthInNumber == '09' || monthInNumber == '9'){month = "September";}
	else if(monthInNumber == '10' || monthInNumber == '10'){month = "October";}
	else if(monthInNumber == '11' || monthInNumber == '11'){month = "November";}
	else if(monthInNumber == '12' || monthInNumber == '12'){month = "December";}
	else {month = "Unknown";}
	
	var dateString = month+", "+year;
	return dateString;
}
function convertToCustomISO(date)
{
	var dateParts = date.split('-');
	var year = dateParts[0];
	var monthInNumber = dateParts[1];
	var day= dateParts[2];
	var month = "";
	if(monthInNumber == '01' || monthInNumber == '1'){month = "January";}
	else if(monthInNumber == '02' || monthInNumber == '2'){month = "February";}
	else if(monthInNumber == '03' || monthInNumber == '3'){month = "March";}
	else if(monthInNumber == '04' || monthInNumber == '4'){month = "April";}
	else if(monthInNumber == '05' || monthInNumber == '5'){month = "May";}
	else if(monthInNumber == '06' || monthInNumber == '6'){month = "June";}
	else if(monthInNumber == '07' || monthInNumber == '7'){month = "July";}
	else if(monthInNumber == '08' || monthInNumber == '8'){month = "August";}
	else if(monthInNumber == '09' || monthInNumber == '9'){month = "September";}
	else if(monthInNumber == '10' || monthInNumber == '10'){month = "October";}
	else if(monthInNumber == '11' || monthInNumber == '11'){month = "November";}
	else if(monthInNumber == '12' || monthInNumber == '12'){month = "December";}
	else {month = "Unknown";}
	
	var dateString = day+" "+month+", "+year;
	return dateString;
}

function download(link)
{
	//href="/download?link='+csrFilePth+'";
	//download
	//alert(link);
	var downloadLink = "/download?link="+link;
	console.log("downloadLink: "+downloadLink);
	//$('#download').attr("href",downloadLink);
	 $(location).attr('href', downloadLink);
	//$("#download").trigger("click");
	//window.location="/download?link="+link;
	//$(elem).attr({target: '_blank', href: "/download?link="});
	/*$.ajax({
		type:"POST",
		url:"/download",
		data:{
			link: link
		},
		success:function(response)
		{
			alert(response);
		},
		error:function(response)
		{},
		complete:function(response)
		{}
	});*/
}