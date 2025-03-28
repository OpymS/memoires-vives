document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('openModalBtn').addEventListener('click', openModal);

	//document.getElementById('closeModalBtn').addEventListener('click', closeModal);
	document.querySelectorAll('.js-close-modal').forEach(a => {
		a.addEventListener('click', closeModal);
	})

	document.addEventListener('keydown', function(e) {
		if (e.key === 'Escape') {
			closeModal(e);
		}
	})

	let latitude = document.getElementById('latitude').value;
	let longitude = document.getElementById('longitude').value;
	let placeName = document.getElementById('locationName').value;

	async function openModal(e) {
		e.preventDefault();
		document.getElementById('modal').classList.remove('hidden');
		latitude = document.getElementById('latitude').value;
		longitude = document.getElementById('longitude').value;
		setTimeout(() => map.invalidateSize(), 200);
	}

	function closeModal(e) {
		e.preventDefault();
		document.getElementById('modal').classList.add('hidden');
	}

	let map = L.map("map", {
		zoom: 10,
		center: [latitude, longitude]
	});

	L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
		minZoom: 1,
		maxZoom: 20,
		attribution: 'données © <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
	}).addTo(map);

	var marker = L.marker([latitude, longitude]).addTo(map);
	marker._icon.classList.add("hue-rotate-180");
	marker.bindPopup(`${placeName} - ${latitude} - ${longitude}`);

	map.on('moveend', fetchPointsInView)

	async function fetchPointsInView() {
		let bounds = map.getBounds();
		let south = bounds.getSouth();
		let north = bounds.getNorth();
		let east = bounds.getEast();
		let west = bounds.getWest();
		document.getElementById('southWest').textContent = `South : ${south} - West : ${west}`;
		document.getElementById('northEast').textContent = `North : ${north} - East : ${east}`;

		const response = await fetch(`/api/location/visible-points?north=${north}&south=${south}&east=${east}&west=${west}`)
		const locations = await response.json();
		console.log(locations);
	}
});