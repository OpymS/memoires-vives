document.addEventListener('DOMContentLoaded', () => {
	const addSourceBtn = document.getElementById('addSourceBtn');
	const sourceInput = document.getElementById('source-input');

	addSourceBtn.addEventListener('click', function(e) {
		e.preventDefault();
		const url = sourceInput.value;
		if (url != null && url != "") {
			const domain = extractDomain(url);
			if (domain != null){
				addSourceToList(url, domain);
			}
		}
		sourceInput.value = "";
	})
});

document.addEventListener('click', function(e) {
	if (e.target.classList.contains("remove-source")) {
		e.target.parentElement.remove();
		reindexSources();
	}
});

function addSourceToList(url, domain) {
	const existing = [...document.querySelectorAll("#sources-container input")]
		.map(i => i.value);

	if (existing.includes(url)) {
		alert("Cette source est déjà ajoutée");
		return;
	}

	const sourceContainer = document.getElementById('sources-container');
	const index = sourceContainer.children.length;
	const sourceDiv = document.createElement('div');
	sourceDiv.className = "source-item flex items-center gap-2 bg-gray-100 text-gray-800 px-3 py-1 rounded-full text-sm shadow-sm";

	sourceDiv.innerHTML = `
		 	<img class="w-4 h-4" src="https://www.google.com/s2/favicons?domain=${domain}&sz=32">
			<span class="source-tag text-slate-700 font-medium">${domain}</span>
        	<button type="button" class="remove-source ml-1 text-slate-400 hover:text-red-500 transition">✕</button>
        	<input type="hidden" name="sources[${index}].url" value="${url}">
        `;

	sourceContainer.appendChild(sourceDiv);
}

function extractDomain(url) {
	try {
		return new URL(url).hostname.replace(/^www\./, "");
	} catch {
		alert("URL invalide")
		return;
	}
}

function reindexSources() {

	const inputs = document.querySelectorAll("#sources-container input");

	inputs.forEach((input, i) => {
		input.name = `sources[${i}]`;
	});

}
