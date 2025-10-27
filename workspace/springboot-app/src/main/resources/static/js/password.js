document.addEventListener('DOMContentLoaded', () => {
	document.querySelectorAll('.password-toggle').forEach((toggle) => {
		toggle.addEventListener('click', function () {
			const input = this.previousElementSibling;
			const isPassword = input.type === 'password';
			input.type = isPassword ? 'text' : 'password';
		});
	});
});
