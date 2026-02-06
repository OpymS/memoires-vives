document.addEventListener('DOMContentLoaded', async () => {
	const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
	const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

	const gridButton = document.getElementById('grid-button');
	const mapButton = document.getElementById('map-button');
	const mapContainer = document.getElementById('map-container');
	const gridContainer = document.getElementById('grid-container');
	const cardsContainer = document.getElementById('cards-container');
	const myMemoriesContainer = document.getElementById('my-memories-container');
	const keyWordsInput = document.getElementById('key-word-input');
	const titleOnlyCheck = document.getElementById('only-title');
	const minDateInput = document.getElementById('after-input');
	const maxDateInput = document.getElementById('before-input');
	const categoriesInput = document.getElementById('category-input');
	const myMemoriesCheck = document.getElementById('my-memories');
	const statusContainer = document.getElementById('status-container');
	const statusInput = document.getElementById('status');
	const resetButton = document.getElementById('reset');

	const pageControl = document.getElementById('pagination-control');

	const isAuthenticated = JSON.parse(document.body.getAttribute('data-authenticated'));

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

	let userLatitude = 0;
	let userLongitude = 0;
	let center = [userLatitude, userLongitude];
	let zoom = 6;

	let selectedMode = 'grid';
	let currentPage = 1;
	let map;
	let markers;
	let mapInited = false;
	let storedCoordinates = false;

	await init();

	async function init() {
		gridButton.addEventListener('click', (e) => { toggleMode('grid') });
		mapButton.addEventListener('click', (e) => { toggleMode('map') });

		keyWordsInput.addEventListener('blur', keyWordsProcess);
		titleOnlyCheck.addEventListener('change', titleOnlyProcess);

		minDateInput.addEventListener('blur', minDateProcess);
		maxDateInput.addEventListener('blur', maxDateProcess);

		statusInput.addEventListener('change', statusProcess);

		categoriesInput.addEventListener('change', categoriesProcess);

		myMemoriesCheck.addEventListener('change', myMemoriesOnly);

		resetButton.addEventListener('click', async function(e) {
			e.preventDefault();
			await resetCriterias();
		});

		if (isAuthenticated) {
			myMemoriesContainer.classList.remove('hidden');
		} else {
			myMemoriesContainer.classList.add('hidden');
		}

		await destockCriterias();
		updateFilters();
		await toggleMode(selectedMode);

		updateMemories();
	}

	async function toggleMode(clicked) {
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
			if (clicked === 'map' && !mapInited) {
				await initMap();
				mapInited = true;
			}

			updateMemories();
		}
	}


	async function updateMemories() {
		if (selectedMode === 'grid') {
			stockCriterias();
			currentPage = 1;
			const serverResponse = await fetchMemories();
			const views = serverResponse.content;
			const lastPageNumber = serverResponse.totalPages;
			updateGrid(views);
			updatePaginationControl(lastPageNumber);
		} else {
			setTimeout(() => map.invalidateSize(), 200);
			const bounds = map.getBounds();
			criterias.north = bounds.getNorth();
			criterias.south = bounds.getSouth();
			criterias.east = bounds.getEast();
			criterias.west = bounds.getWest();
			stockCriterias();
			const memories = await fetchMemoriesOnMap();
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
			const views = await response.json();
			return views;
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
			const views = await response.json();
			return views;
		} catch (error) {
			console.error('Error:', error);
			return [];
		}
	}

	function updateGrid(views) {
		cardsContainer.innerHTML = '';
		views.forEach((view) => {
			const memory= view.memory;
			const memoryDiv = document.createElement('a');
			memoryDiv.href = view.canonicalUrl;
			memoryDiv.className = 'border border-black rounded-lg flex flex-col items-center w-full h-[150vw] md:h-[30vw] lg:h-[20vw] overflow-hidden duration-200 hover:scale-101';
			if (memory.mediaUUID) {
				memoryDiv.innerHTML = `<div class="relative w-full pb-[100%] border-b">
							<img class="absolute rounded-t-lg w-full h-full object-cover" src="/uploads/${memory.mediaUUID}" alt="illustration de ${memory.title}" loading="lazy"/>
						</div>
						<h3 class="h3-title mb-2">${memory.title}</h3>
						<p class="w-full px-1 text-justify">${memory.description}</p>
				`;
			} else {
				memoryDiv.innerHTML = `<div class="relative w-full pb-[100%] border-b">
						<img class="absolute rounded-t-lg w-full h-full object-cover" src="/images/public/memory-placeholder.png" alt="souvenir sans illustration" loading="lazy"/>
					</div>
					<h3 class="h3-title mb-2">${memory.title}</h3>
					<p class="w-full px-1 font-light text-justify">${memory.description}</p>
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
				pageLink.className = 'font-bold text[#7e9076] cursor-default';
			} else {
				pageLink.className = 'font-light text-[#bddab2] cursor-pointer hover:text-white hover:bg-[#bddab2] hover:font-bold';
				pageLink.addEventListener('click', () => {
					goToPage(page);
				});
			}
			pageControl.appendChild(pageLink);

		}
	}

	function updateMap(views) {
		markers.clearLayers();
		views.forEach(view => {
			const memory = view.memory;
			const marker = L.marker([memory.location.latitude, memory.location.longitude]).addTo(markers);
			const memoryDiv = document.createElement('a');
			memoryDiv.href = view.canonicalUrl;
			memoryDiv.className = 'flex flex-row h-[200px] w-[350px] items-center p-2 gap-4';
			if (memory.mediaUUID) {
				memoryDiv.innerHTML = `<div class="w-[100px] h-[100px] rounded-[50%] overflow-hidden flex-shrink-0">
							<img class="w-full h-full object-cover" src="/uploads/${memory.mediaUUID}" alt="illustration de ${memory.title}" loading="lazy"/>
						</div>`;
			} else {
				memoryDiv.innerHTML = `<div class="w-[100px] h-[100px] rounded-[50%] overflow-hidden flex-shrink-0">
							<img class="w-full h-full object-cover" src="/images/public/memory-placeholder.png" alt="souvenir sans illustration" loading="lazy"/>
						</div>`;
			}
			memoryDiv.innerHTML += `<div class="h-full flex-1 min-w-0 flex flex-col text-black justify-start items-start">
							<h3 class="text-xl text-center">${memory.title}</h3>
							<p class="w-full font-light text-justify overflow-y-scroll wrap-break-word">${memory.description}</p>
						</div>`;
			marker.on('click', function() {
				openPopup(marker, memoryDiv);
			});
		})
	}

	async function goToPage(pageNumber) {
		currentPage = pageNumber;
		const serverResponse = await fetchMemories(pageNumber);
		const views = serverResponse.content;
		const lastPageNumber = serverResponse.totalPages;
		updateGrid(views);
		updatePaginationControl(lastPageNumber);
		stockCriterias();
	}

	function stockCriterias() {
		localStorage.setItem('criterias', JSON.stringify(criterias));
		localStorage.setItem('mode', JSON.stringify(selectedMode));
		if (selectedMode === 'map') {
			localStorage.setItem('zoom', JSON.stringify(map.getZoom()));
			localStorage.setItem('center', JSON.stringify(map.getCenter()));
		} else {
			localStorage.setItem('page', JSON.stringify(currentPage));
		}
	}

	async function destockCriterias() {
		const storedCriterias = localStorage.getItem('criterias');
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

		const storedCenter = localStorage.getItem('center');
		const storedZoom = localStorage.getItem('zoom');
		const storedPage = localStorage.getItem('page');
		if (storedCenter) {
			center = JSON.parse(storedCenter);
			storedCoordinates = true;
		}

		if (storedZoom) {
			zoom = JSON.parse(storedZoom);
		}
		if (storedPage) {
			currentPage = JSON.parse(storedPage);
		}

		const storedMode = localStorage.getItem('mode');
		if (storedMode) {
			await toggleMode(JSON.parse(storedMode));
		}
	}

	async function initMap() {
		if (!storedCoordinates) {
			await getPositionByIP();
			center = [userLatitude, userLongitude];
		}
		map = L.map("map", {
			worldCopyJump: true,
			zoom: zoom,
			center: center
		});

		L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
			minZoom: 1,
			maxZoom: 20,
			attribution: 'données © <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
		}).addTo(map);

		markers = L.layerGroup().addTo(map);

		map.on('moveend', updateMemories);

		mapInited = true;

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

	function updateFilters() {
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
		} else {
			for (let i = 1; i < categoriesInput.options.length; i++) {
				categoriesInput.options[i].selected = false;
			}
		}


		myMemoriesCheck.checked = criterias.onlyMine;
		if (myMemoriesCheck.checked) {
			statusContainer.classList.remove('hidden');
			statusInput.value = criterias.status;
		} else {
			statusContainer.classList.add('hidden');
			statusInput.value = 1;
		}
	}

	async function resetCriterias() {
		criterias.words = null;
		criterias.titleOnly = false;
		criterias.after = null;
		criterias.before = null;
		criterias.categoriesId = null;
		criterias.onlyMine = false;
		criterias.status = 1;
		criterias.north = 1.0;
		criterias.south = -1.0;
		criterias.east = 1.0;
		criterias.west = -1.0;


		center = [userLatitude, userLongitude];

		zoom = 6;

		currentPage = 1;
		storedCoordinates = false;

		stockCriterias();
		updateFilters();
		await toggleMode('grid');

		updateMemories();
	}

	function openPopup(marker, popupContent) {
		map.off('moveend', updateMemories);

		setTimeout(function() {
			marker.bindPopup(popupContent,{maxWidth:350}).openPopup();
		}, 100);

		map.once('popupclose', function() {
			map.on('moveend', updateMemories);
		});
	}

	async function getPositionByIP() {
		const response = await fetch('https://ipapi.co/json/');
		const data = await response.json();
		userLatitude = data.latitude;
		userLongitude = data.longitude;
	}
});

//if (!storedCoordinates && "geolocation" in navigator) {
//				const position = await new Promise((resolve, reject) => {
//					navigator.geolocation.getCurrentPosition(resolve, reject);
//				});
//
//				userLatitude = position.coords.latitude;
//				userLongitude = position.coords.longitude;
//				center = [userLatitude, userLongitude];
//				map.flyTo(center, zoom);
//			}