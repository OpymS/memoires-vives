document.addEventListener('DOMContentLoaded', () => {

	document.getElementById('imageInput').addEventListener('change', function(e) {
		const file = e.target.files[0];
		const reader = new FileReader();

		reader.onload = function(e) {
			document.getElementById('image').src = e.target.result;
		};

		reader.readAsDataURL(file);
	})
});
