function loginSubmit(token) {
	document.getElementById("12-form").submit();
}

function addSubmit(token) {
	document.getElementById("1-form").submit();
}

function editSubmit(token) {
	document.getElementById("5-form").submit();
}

function additionHandler(e) {
	e.preventDefault();
    grecaptcha.ready(function() {
		var recaptchaSiteKey = document.querySelector("meta[name='recaptchaSiteKey']").getAttribute("content");
    	grecaptcha.execute(recaptchaSiteKey, {action: 'addition'}).then(function(token) {
			e.target.parentNode.elements['recaptcha'].value = token;
			e.target.parentNode.submit();
		});
	});
}

function removeAdditionHandler(e) {
	e.preventDefault();
    grecaptcha.ready(function() {
		var recaptchaSiteKey = document.querySelector("meta[name='recaptchaSiteKey']").getAttribute("content");
    	grecaptcha.execute(recaptchaSiteKey, {action: 'removeAddition'}).then(function(token) {
			e.target.parentNode.elements['recaptcha'].value = token;
			e.target.parentNode.submit();
		});
	});
}

function deleteHandler(e) {
	e.preventDefault();
    grecaptcha.ready(function() {
		var recaptchaSiteKey = document.querySelector("meta[name='recaptchaSiteKey']").getAttribute("content");
    	grecaptcha.execute(recaptchaSiteKey, {action: 'delete'}).then(function(token) {
			e.target.parentNode.elements['recaptcha'].value = token;
			e.target.parentNode.submit();
		});
	});
}

function deletionHandler(e) {
	e.preventDefault();
    grecaptcha.ready(function() {
		var recaptchaSiteKey = document.querySelector("meta[name='recaptchaSiteKey']").getAttribute("content");
    	grecaptcha.execute(recaptchaSiteKey, {action: 'deletion'}).then(function(token) {
			e.target.parentNode.elements['recaptcha'].value = token;
			e.target.parentNode.submit();
		});
	});
}

function removeDeletionHandler(e) {
	e.preventDefault();
    grecaptcha.ready(function() {
		var recaptchaSiteKey = document.querySelector("meta[name='recaptchaSiteKey']").getAttribute("content");
    	grecaptcha.execute(recaptchaSiteKey, {action: 'removeDeletion'}).then(function(token) {
			e.target.parentNode.elements['recaptcha'].value = token;
			e.target.parentNode.submit();
		});
	});
}