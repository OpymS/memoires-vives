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
//	console.log(placeName);
	if (placeName === '' && "geolocation" in navigator){
		console.log('plop');
		navigator.geolocation.getCurrentPosition((userPosition) => {
			latitude = userPosition.coords.latitude;
			longitude = userPosition.coords.longitude;
		})
		console.log(" latitude : " + latitude);
		console.log("longitude : " + longitude);
	}

	async function openModal(e) {
		e.preventDefault();
		document.getElementById('modal').classList.remove('hidden');
		map.panTo(new L.LatLng(latitude, longitude));
		setTimeout(() => map.invalidateSize(), 200);
	}

	function closeModal(e) {
		e.preventDefault();
		document.getElementById('modal').classList.add('hidden');
	}

	let map = L.map("map", {
		worldCopyJump: true,
		zoom: 5,
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

	map.on('click', addLocation)

	async function fetchPointsInView() {

		let bounds = map.getBounds();
		let north = bounds.getNorth();
		let south = bounds.getSouth();
		let east = bounds.getEast();
		let west = bounds.getWest();
		document.getElementById('southWest').textContent = `South : ${south} - West : ${west}`;
		document.getElementById('northEast').textContent = `North : ${north} - East : ${east}`;
		//		north = 90;
		//		south = -90;
		//		east = -160;
		//		west = 160;
		const response = await fetch(`/api/location/visible-points?north=${north}&south=${south}&east=${east}&west=${west}`)
		const locations = await response.json();
		console.log(locations);
		locations.forEach(location => {
			if (location.name != placeName) {
				var marker = L.marker([location.latitude, location.longitude]).addTo(map);
				marker.bindPopup(`${location.name} - ${location.latitude} - ${location.longitude}`);
			}
		})
	}

	function addLocation(e) {
		console.log(" latitude : " + e.latlng.lat);
		console.log("longitude : " + e.latlng.lng);
	}
});