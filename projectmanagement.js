var xhr;	 
function UpladFile(){
	 var fileObj = document.getElementById("file").files[0];
	 var form = new FormData(); 
	 var pageoid=document.getElementById("pageoid").value;
	 var ibookid=document.getElementById("ibookid").value;
	 if(fileObj==null){
		alert("请选择文件后再上传");
		return;
	 }
	 var url="/Windchill/ptc1/ext/plm/component/planActivity?pageoid="+pageoid+"&action=upload&ibookid="+ibookid;
	 form.append("file", fileObj);     	 
	 if(window.ActiveXObject){ //如果是IE浏览器    
        xhr= new ActiveXObject("Microsoft.XMLHTTP");    
		}
	 else if(window.XMLHttpRequest){     
        xhr= new XMLHttpRequest();    
     }  
     xhr.open("post", url, true); 
    // xhr.onload = uploadComplete;
	 xhr.onreadystatechange=callback; 
     xhr.onerror =  uploadFailed; 
     xhr.send(form); 
            
}
function callback() {
    if(xhr.readyState == 4) {  
		if(xhr.status == 200) {  
			var response = xhr.responseText;  
			//var result = request.responseText;  
		//	alert(response); 
			var jsonObject=eval("("+response+")"); 
			var action =jsonObject.action;
		//	alert(action);
			if(jsonObject.action=="clear"){
				if(jsonObject.success=="false"){
					alert(jsonObject.result);
					return;
				}
				var link= document.getElementById("linkid");	
				link.innerHTML="";	
				return;
			}
		    
			var link =document.getElementById("linkid");
			var url =jsonObject.url;
			//alert(url);
			link.innerHTML="<a href="+url+" style=\"color:blue; \">"+jsonObject.itemName+"</a>";
			link.style.color = "red";
			
			var ibookid =document.getElementById("ibookid");
			ibookid.value=jsonObject.ibookid;

			/**
			var input = document.createElement("input");
			input.type = "button";
			input.value = "删除";
			input.id="clearbn";
			link.appendChild(input);
            var cbn=document.getElementById("clearbn");
			cbn.click();
			cbn.onclick = function(){
   				alert(3);
 			};
           */
        }  
    }  
}
function uploadFailed(evt) {
            alert("上传失败！");
        }
function clearFile(){
	var form = new FormData(); 

	var pageoid=document.getElementById("pageoid").value;
	var ibookid=document.getElementById("ibookid").value;
	alert(ibookid);
	var url="/Windchill/ptc1/ext/plm/component/planActivity?pageoid="+pageoid+"&action=clear&ibookid=OR:"+ibookid;
	//form.append("file", fileObj);     	 
	if(window.ActiveXObject){     
        xhr= new ActiveXObject("Microsoft.XMLHTTP");    
		}
	 else if(window.XMLHttpRequest){   
        xhr= new XMLHttpRequest();    
     }  
     xhr.open("post", url, true); 
     xhr.onreadystatechange=callback; 
     xhr.onerror =  uploadFailed; 
     xhr.send(form);

	 }
