document.addEventListener('DOMContentLoaded', () => {
	const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
	const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

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

	const nextPage = document.getElementById('page-2');

	const criterias = {
		words: null,
		titleOnly: false,
		after: null,
		before: null,
		categories: null,
		onlyMine: false,
		status: 1
	};

	nextPage.addEventListener('click', getNextPage);

	let selectedMode = 'grid';

	gridButton.addEventListener('click', (e) => { toggleMode('grid') });
	mapButton.addEventListener('click', (e) => { toggleMode('map') });

	keyWordsInput.addEventListener('blur', keyWordsProcess);
	titleOnlyCheck.addEventListener('change', titleOnlyProcess);

	minDateInput.addEventListener('blur', minDateProcess);
	maxDateInput.addEventListener('blur', maxDateProcess);

	statusInput.addEventListener('change', statusProcess);

	categoriesInput.addEventListener('change', categoriesProcess);

	myMemoriesCheck.addEventListener('change', myMemoriesOnly);

	function toggleMode(clicked) {
		if (clicked != selectedMode) {
			mapContainer.classList.toggle("hidden");
			gridContainer.classList.toggle("hidden");
			mapButton.classList.toggle("bg-white/50");
			mapButton.classList.toggle("border-y-2");
			mapButton.classList.toggle("border-r-2");
			gridButton.classList.toggle("bg-white/50");
			gridButton.classList.toggle("border-y-2");
			gridButton.classList.toggle("border-l-2");
			selectedMode = selectedMode == 'grid' ? 'map' : 'grid';
		}
	}

	function myMemoriesOnly() {
		if (this.checked) {
			statusContainer.classList.remove('hidden');
			criterias.onlyMine = true;
		} else {
			statusContainer.classList.add('hidden');
			criterias.onlyMine = false;
		}
	}

	async function keyWordsProcess() {
		const wordsToFind = this.value.split(',');
		criterias.words = wordsToFind.map(word => word.trim());
		await fetchMemories();
	}

	function titleOnlyProcess() {
		console.log(`la case a été cliquée. Elle est maintenant ${this.checked ? 'cochée' : 'décochée'}`);
	}

	function minDateProcess() {
		console.log(this.value);
	}

	function maxDateProcess() {
		console.log(this.value);
	}

	function categoriesProcess() {
		const selectedCategories = [];
		for (i = 0; i < this.options.length; i++) {
			if (this.options[i].selected) {
				selectedCategories.push(this.options[i].value);
			}
		}
		console.log(selectedCategories);
	}

	function statusProcess() {
		console.log(this.value);
	}

	async function getNextPage() {
		const memories = await fetchMemories();
		console.log(memories);
	}

	async function fetchMemories() {
		const url = `/api/memory/grid?pageNumber=${encodeURIComponent(2)}`;
		
		try {
			const response = await fetch(url, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					[csrfHeader]: csrfToken
				},
				body: JSON.stringify(criterias),
				credentials: "include"
			});
			console.log(response);
			if (!response.ok) {
				throw new Error(response.status === 403 ? 'Problème.' : 'An error occurred.');
			}
			const memories = await response.json();
			return memories;
		} catch (error) {
			console.error('Error:', error);
			return [];
		}
	}

	//	async function fetchMemories() {
	//		const url = `/api/memory/grid?pageNumber=${encodeURIComponent(2)}`;
	//		
	//		try {
	//			const response = await fetch(url);
	//			console.log(response);
	//			if (!response.ok) {
	//				throw new Error(response.status === 403 ? 'Problème.' : 'An error occurred.');
	//			}
	//			const memories = await response.json();
	//			return memories;
	//		} catch (error) {
	//			console.error('Error:', error);
	//			return [];
	//		}
	//	}
});
