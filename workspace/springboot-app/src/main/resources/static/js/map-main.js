document.addEventListener('DOMContentLoaded', () => {
	const gridButton = document.getElementById('grid');
	const mapButton = document.getElementById('map');
	const mapContainer = document.getElementById('map-container');
	const gridContainer = document.getElementById('grid-container');

	let selected = 'grid';

	gridButton.addEventListener('click', (event) => { toggleMode('grid') });
	mapButton.addEventListener('click', (event) => { toggleMode('map') });

	function toggleMode(clicked) {
		if (clicked != selected) {
			mapContainer.classList.toggle("hidden");
			gridContainer.classList.toggle("hidden");
			mapButton.classList.toggle("bg-white/50");
			mapButton.classList.toggle("border-y-2");
			mapButton.classList.toggle("border-r-2");
			gridButton.classList.toggle("bg-white/50");
			gridButton.classList.toggle("border-y-2");
			gridButton.classList.toggle("border-l-2");
			selected = selected == 'grid' ? 'map' : 'grid';
		}
	}

});
