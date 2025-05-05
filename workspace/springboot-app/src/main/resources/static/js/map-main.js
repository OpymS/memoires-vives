document.addEventListener('DOMContentLoaded', () => {
	const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
	const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

	const gridButton = document.getElementById('grid');
	const mapButton = document.getElementById('map');
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
		status: 1
	};

//	nextPage.addEventListener('click', getNextPage);

	let selectedMode = 'grid';
	let currentPage = 1;

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
		for (i = 0; i < this.options.length; i++) {
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

	async function getNextPage() {
		const memories = await updateMemories();
		console.log(memories);
	}

	async function updateMemories() {
		const serverResponse = await fetchMemories();
		const memories = serverResponse.content;
		const lastPageNumber = serverResponse.totalPages;
		updateGrid(memories);
		updatePaginationControl(lastPageNumber);
	}

	async function fetchMemories(page) {
		//		const url = `/api/memory/grid?pageNumber=${encodeURIComponent(2)}`;
		let url;
		if (page){
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
	
	async function goToPage(pageNumber){
		currentPage = pageNumber;
		const serverResponse = await fetchMemories(pageNumber);
		const memories = serverResponse.content;
		const lastPageNumber = serverResponse.totalPages;
		updateGrid(memories);
		updatePaginationControl(lastPageNumber);
	}
});
