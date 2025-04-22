document.addEventListener('DOMContentLoaded', () => {
	const toggleMenuBtn = document.getElementById('menu-btn');
	const toggleMenuImg = document.querySelector('#menu-btn img');
	const menu = document.getElementById('toggled-menu');
	const navBar = document.getElementById('nav');

	toggleMenuBtn.addEventListener('click', toggleMenu);

	function toggleMenu() {
		const menuStatus = toggleMenuBtn.getAttribute('aria-expanded');
		if (menuStatus == 'false') {
			toggleMenuImg.setAttribute("src", "/images/public/cross.svg")
			toggleMenuBtn.setAttribute('aria-expanded', true);
		} else {
			toggleMenuImg.setAttribute("src", "/images/public/menu.svg")
			toggleMenuBtn.setAttribute('aria-expanded', false);
		}

		menu.classList.toggle("max-sm:-translate-y-full");
		menu.classList.toggle("max-sm:top-0");
		menu.classList.toggle("max-sm:top-24");
		navBar.classList.toggle("border-b");
	}

});
