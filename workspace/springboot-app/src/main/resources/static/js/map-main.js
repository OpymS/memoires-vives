document.addEventListener('DOMContentLoaded', () => {
	const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
	const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

	const gridButton = document.getElementById('grid-button');
	const mapButton = document.getElementById('map-button');
	const mapContainer = document.getElementById('map-container');
	const gridContainer = document.getElementById('grid-container');
	const cardsContainer = document.getElementById('cards-container');
	const keyWordsInput = document.getElementById('key-word-input');
	const titleOnlyCheck = document.getElementById('only-title');
	const minDateInput = document.getElementById('after-input');
	const maxDateInput = document.getElementById('before-input');
	const categoriesInput = document.getElementById('category-input');
	const myMemoriesCheck = document.getElementById('my-memories');
	const statusContainer = document.getElementById('status-container');
	const statusInput = document.getElementById('status');

	const pageControl = document.getElementById('pagination-control');

	const criterias = {
		words: null,
		titleOnly: false,
		after: null,
		before: null,
		categoriesId: null,
		onlyMine: false,
		status: 1,
		north: 1.0,
		south: -1.0,
		east: 1,
		west: -1
	};

	let userLatitude = 32.324;
	let userLongitude = 0.754;

	let selectedMode = 'grid';
	let currentPage = 1;
	let map;

	init();

	function init() {
		gridButton.addEventListener('click', (e) => { toggleMode('grid') });
		mapButton.addEventListener('click', (e) => { toggleMode('map') });

		keyWordsInput.addEventListener('blur', keyWordsProcess);
		titleOnlyCheck.addEventListener('change', titleOnlyProcess);

		minDateInput.addEventListener('blur', minDateProcess);
		maxDateInput.addEventListener('blur', maxDateProcess);

		statusInput.addEventListener('change', statusProcess);

		categoriesInput.addEventListener('change', categoriesProcess);

		myMemoriesCheck.addEventListener('change', myMemoriesOnly);

		destockCriterias();

		if (criterias.words) {
			keyWordsInput.value = criterias.words.join(', ');
		}
		titleOnlyCheck.checked = criterias.titleOnly;
		minDateInput.value = criterias.after;
		maxDateInput.value = criterias.before;
		if (criterias.categoriesId) {
			for (let i = 1; i < categoriesInput.options.length; i++) {
				categoriesInput.options[i].selected = criterias.categoriesId.includes(categoriesInput.options[i].value);
			}
		}


		myMemoriesCheck.checked = criterias.onlyMine;
		if (myMemoriesCheck.checked) {
			statusInput.value = criterias.status;
		} else {
			statusInput.value = 1;
		}

		updateMemories();

		map = L.map("map", {
			worldCopyJump: true,
			zoom: 6,
			center: [userLatitude, userLongitude]
		});

		L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
			minZoom: 1,
			maxZoom: 20,
			attribution: 'données © <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
		}).addTo(map);

		map.on('load', function() {
			console.log("Carte chargée");
		});
	}

	function toggleMode(clicked) {
		if (clicked !== selectedMode) {
			mapContainer.classList.toggle("hidden");
			gridContainer.classList.toggle("hidden");
			mapButton.classList.toggle("bg-white/50");
			mapButton.classList.toggle("border-y-2");
			mapButton.classList.toggle("border-r-2");
			gridButton.classList.toggle("bg-white/50");
			gridButton.classList.toggle("border-y-2");
			gridButton.classList.toggle("border-l-2");
			selectedMode = selectedMode === 'grid' ? 'map' : 'grid';

			updateMemories();
		}
	}


	async function updateMemories() {
		if (selectedMode === 'grid') {
			stockCriterias();
			currentPage = 1;
			const serverResponse = await fetchMemories();
			const memories = serverResponse.content;
			const lastPageNumber = serverResponse.totalPages;
			updateGrid(memories);
			updatePaginationControl(lastPageNumber);
		} else {
			setTimeout(() => map.invalidateSize(), 200);
			const bounds = map.getBounds();
			criterias.north = bounds.getNorth();
			criterias.south = bounds.getSouth();
			criterias.east = bounds.getEast();
			criterias.west = bounds.getWest();
			stockCriterias();
			console.log(criterias);
			const memories = await fetchMemoriesOnMap();
			console.log(memories);
			updateMap(memories);
		}
	}

	async function fetchMemories(page) {
		let url;
		if (page) {
			url = `/api/memory/grid?pageNumber=${page}`;
		} else {
			url = `/api/memory/grid`;
		}

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

	async function fetchMemoriesOnMap() {
		let url = '/api/memory/map';

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

	function updateGrid(memories) {
		cardsContainer.innerHTML = '';
		memories.forEach((memory) => {
			const memoryDiv = document.createElement('a');
			memoryDiv.href = `/memory?memoryId=${memory.memoryId}`;
			memoryDiv.className = 'border border-black rounded-lg flex flex-col items-center w-full h-[150vw] md:h-[30vw] lg:h-[20vw] overflow-hidden';
			if (memory.mediaUUID) {
				memoryDiv.innerHTML = `<div class="relative w-full pb-[100%] border-b">
							<img class="absolute rounded-t-lg w-full h-full object-cover" src="/uploads/${memory.mediaUUID}" alt="illustration de ${memory.title}"/>
						</div>
						<h3 class="text-xl mx-2 text-center">${memory.title}</h3>
						<h4 class="w-full px-1 font-light text-justify">${memory.description}</h4>
				`;
			} else {
				memoryDiv.innerHTML = `<div class="relative w-full pb-[100%] border-b">
						<img class="absolute rounded-t-lg w-full h-full object-cover" src="/images/public/memory-placeholder.png" alt="illustration de ${memory.title}" />
					</div>
					<h3 class="text-xl mx-2 text-center">${memory.title}</h3>
					<h4 class="w-full px-1 font-light text-justify">${memory.description}</h4>
			`;
			}
			cardsContainer.appendChild(memoryDiv);
		});
	}

	function updatePaginationControl(lastPageNumber) {
		pageControl.innerHTML = '';
		for (let page = 1; page <= lastPageNumber; page++) {
			const pageLink = document.createElement('span');
			pageLink.textContent = page;
			if (page === currentPage) {
				pageLink.className = 'font-bold text[#7e9076]';
			} else {
				pageLink.className = 'font-light text-[#bddab2] hover:text-white hover:bg-[#bddab2] hover:font-bold';
				pageLink.addEventListener('click', () => {
					goToPage(page);
				});
			}
			pageControl.appendChild(pageLink);

		}
	}

	function updateMap(memories) {
		memories.forEach(memory => {
			const marker = L.marker([memory.location.latitude, memory.location.longitude]).addTo(map);
			marker.bindPopup(`${memory.title}`);
		})
	}

	async function goToPage(pageNumber) {
		currentPage = pageNumber;
		const serverResponse = await fetchMemories(pageNumber);
		const memories = serverResponse.content;
		const lastPageNumber = serverResponse.totalPages;
		updateGrid(memories);
		updatePaginationControl(lastPageNumber);
	}

	function stockCriterias() {
		sessionStorage.setItem('criterias', JSON.stringify(criterias));
	}

	function destockCriterias() {
		const storedCriterias = sessionStorage.getItem('criterias');
		if (storedCriterias) {
			const parsedCriterias = JSON.parse(storedCriterias);
			criterias.words = parsedCriterias.words;
			criterias.titleOnly = parsedCriterias.titleOnly;
			criterias.after = parsedCriterias.after;
			criterias.before = parsedCriterias.before;
			criterias.categoriesId = parsedCriterias.categoriesId;
			criterias.onlyMine = parsedCriterias.onlyMine;
			criterias.status = parsedCriterias.status;
		}
	}


	async function myMemoriesOnly() {
		if (this.checked) {
			statusContainer.classList.remove('hidden');
			criterias.onlyMine = true;
			await updateMemories();
		} else {
			statusContainer.classList.add('hidden');
			criterias.onlyMine = false;
			criterias.status = 1;
			await updateMemories();
		}
	}

	async function keyWordsProcess() {
		const wordsToFind = this.value.split(',');
		criterias.words = wordsToFind.map(word => word.trim());
		await updateMemories();
	}

	async function titleOnlyProcess() {
		criterias.titleOnly = this.checked;
		await updateMemories();
	}

	async function minDateProcess() {
		criterias.after = this.value;
		await updateMemories();
	}

	async function maxDateProcess() {
		criterias.before = this.value;
		await updateMemories();
	}

	async function categoriesProcess() {
		const selectedCategories = [];
		for (i = 1; i < this.options.length; i++) {
			if (this.options[i].selected) {
				selectedCategories.push(this.options[i].value);
			}
		}
		criterias.categoriesId = selectedCategories;
		await updateMemories();
	}

	async function statusProcess() {
		criterias.status = this.value;
		await updateMemories();
	}
});
