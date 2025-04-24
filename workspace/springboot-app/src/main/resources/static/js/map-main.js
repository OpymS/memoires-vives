document.addEventListener('DOMContentLoaded', () => {
	const gridButton = document.getElementById('grid');
	const mapButton = document.getElementById('map');
	const mapContainer = document.getElementById('map-container');
	const gridContainer = document.getElementById('grid-container');
	const keyWordsInput = document.getElementById('key-word-input');
	const titleOnlyCheck = document.getElementById('only-title');
	const minDateInput = document.getElementById('after-input');
	const maxDateInput = document.getElementById('before-input');
	const categoriesInput = document.getElementById('category-input');
	const myMemoriesCheck = document.getElementById('my-memories');
	const statusContainer = document.getElementById('status-container');
	const statusInput = document.getElementById('status');

	let selected = 'grid';

	gridButton.addEventListener('click', (e) => { toggleMode('grid') });
	mapButton.addEventListener('click', (e) => { toggleMode('map') });
	
	keyWordsInput.addEventListener('blur', keyWordsProcess);
	titleOnlyCheck.addEventListener('change', titleOnlyProcess);

	myMemoriesCheck.addEventListener('change', myMemoriesOnly);

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

	function myMemoriesOnly() {
		if (this.checked) {
			statusContainer.classList.remove('hidden');
		} else {
			statusContainer.classList.add('hidden');
		}
	}
	
	function keyWordsProcess() {
		const words = this.value.split(',');
		words.forEach((word)=> console.log(word));
	}
	
	function titleOnlyProcess() {
		console.log(`la case a été cliquée. Elle est maintenant ${this.checked?'cochée':'décochée'}`);
	}
});
