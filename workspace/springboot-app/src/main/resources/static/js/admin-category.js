document.addEventListener('DOMContentLoaded', () => {
	const buttons = document.querySelectorAll('[id^="showBtn-"]');
	const memoryCache = {};

	buttons.forEach(button => {
		button.addEventListener('click', toggleMemories);
	});

	async function fetchMemories(categoryId) {
		if (memoryCache[categoryId]) {
			return memoryCache[categoryId];
		}

		try {
			const response = await fetch(`/api/category/${categoryId}/associatedMemories`);
			if (!response.ok) {
				throw new Error(response.status === 403 ? 'Access forbidden: Category not found or access denied.' : 'An error occurred.');
			}
			const memories = await response.json();
			memoryCache[categoryId] = memories;
			return memories;
		} catch (error) {
			console.error('Error:', error);
			return [];
		}
	}

	async function toggleMemories(e) {
		e.preventDefault();
		const button = e.target;
		const box = button.parentNode;
		const categoryId = button.id.substring(8);
		const container = document.getElementById(`memories-${categoryId}`);

		if (button.innerText === 'Voir') {
			button.innerText = 'Masquer';
			if (!container) {
				const memories = await fetchMemories(categoryId);
				const newContainer = document.createElement('div');
				newContainer.id = `memories-${categoryId}`;
				newContainer.classList.add('memories-container');
				memories.forEach(memory => {
					const line = document.createElement('p');
					const memoryLink = document.createElement('a');
					memoryLink.href = `/memory/${memory.memoryId}-${memory.slug}`;
					memoryLink.textContent = memory.title;
					memoryLink.classList.add('hover:underline');
					line.append(memoryLink);
					newContainer.append(line);
				});
				button.insertAdjacentElement('beforebegin', newContainer);
			} else {
				container.classList.remove('hidden');
			}
			box.firstElementChild.classList.add('hidden');
		} else {
			button.innerText = 'Voir';
			container.classList.add('hidden');
			box.firstElementChild.classList.remove('hidden');
		}
	}
});