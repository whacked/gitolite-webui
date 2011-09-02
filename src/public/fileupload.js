var W3CDOM = (document.createElement && document.getElementsByTagName);

function initFileUploads() {
	if (!W3CDOM) return;
	var fakeFileUpload = document.createElement('div');
	fakeFileUpload.className = 'fakefile';
	var fakeInput = document.createElement('input');
      fakeFileUpload.appendChild(fakeInput);
	var image = document.createElement('img');
	image.src='pix/button_select.gif';
	fakeFileUpload.appendChild(image);
	var x = document.getElementsByTagName('input');
	for (var i=0;i<x.length;i++) {
		if (x[i].type != 'file') continue;
		if (x[i].parentNode.className != 'fileinputs') continue;

            if(x[i].className.indexOf('error')!=-1){
                fakeInput.className='error';	    
            } else {
               fakeInput.className='';
            }
           
            fakeInput.value = x[i].getAttribute('value');
		x[i].className = 'file hidden';
		
		var clone = fakeFileUpload.cloneNode(true);
		x[i].parentNode.appendChild(clone);
		x[i].relatedElement = clone.getElementsByTagName('input')[0];
		x[i].onchange = x[i].onmouseout = function () {
                if(this.value){
			this.relatedElement.value = this.value;
               } else {
			this.relatedElement.value = this.getAttribute('value');
               }
		}
	}
}
