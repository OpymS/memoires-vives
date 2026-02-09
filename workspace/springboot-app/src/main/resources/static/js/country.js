document.addEventListener('DOMContentLoaded', async () => {
	const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
	const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

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
	const country = document.getElementById('country-input').value;
	const city = document.getElementById('city-input').value;

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
		west: -1,
		countrySlug: country,
		citySlug: city,
	};


	let currentPage = 1;

	await init();

	async function init() {
		console.log(criterias);
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

		updateFilters();

		updateMemories();
	}


	async function updateMemories() {
		currentPage = 1;
		const serverResponse = await fetchMemories();
		const views = serverResponse.content;
		const lastPageNumber = serverResponse.totalPages;
		updateGrid(views);
		updatePaginationControl(lastPageNumber);

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

	function updateGrid(views) {
		cardsContainer.innerHTML = '';
		views.forEach((view) => {
			const memory = view.memory;
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

	async function goToPage(pageNumber) {
		currentPage = pageNumber;
		const serverResponse = await fetchMemories(pageNumber);
		const views = serverResponse.content;
		const lastPageNumber = serverResponse.totalPages;
		updateGrid(views);
		updatePaginationControl(lastPageNumber);
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
		currentPage = 1;

		updateFilters();

		updateMemories();
	}
});