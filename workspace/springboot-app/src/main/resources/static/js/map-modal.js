document.addEventListener('DOMContentLoaded', () => {
	let latitude;
	let longitude;
	let newLatitude;
	let newLongitude;
	let userLat = 0;
	let userLong = 0;
	let latitudeInput;
	let longitudeInput;
	let placeName;
	let map;
	var marker;

	init();

	function openModal(e) {
		e.preventDefault();
		document.getElementById('modal').classList.remove('hidden');
		if (placeName === '') {
			map.panTo(new L.LatLng(userLat, userLong));
		}
		setTimeout(() => map.invalidateSize(), 200);
	}

	function closeModal(e) {
		e.preventDefault();
		document.getElementById('modal').classList.add('hidden');
	}


	function init() {
		latitudeInput = document.getElementById('latitude');
		longitudeInput = document.getElementById('longitude');
		latitude = latitudeInput.value;
		longitude = longitudeInput.value;
		placeName = document.getElementById('locationName').value;

		initModal();
		initMap();

	}

	function initModal() {
		document.getElementById('openModalBtn').addEventListener('click', openModal);

		document.querySelectorAll('.js-close-modal').forEach(a => {
			a.addEventListener('click', closeModal);
		})

		document.addEventListener('keydown', function(e) {
			if (e.key === 'Escape') {
				closeModal(e);
			}
		})
		
		document.getElementById('validateLocation').addEventListener('click', validateLocation);
	}

	function initMap() {

		if ("geolocation" in navigator) {
			navigator.geolocation.getCurrentPosition((userPosition) => {
				userLat = userPosition.coords.latitude;
				userLong = userPosition.coords.longitude;
			});
		}

		map = L.map("map", {
			worldCopyJump: true,
			zoom: 5,
			center: [latitude, longitude]
		});

		L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
			minZoom: 1,
			maxZoom: 20,
			attribution: 'données © <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
		}).addTo(map);

		if (placeName !== '') {
			marker = L.marker([latitude, longitude], { draggable: 'true', autoPan: 'true' }).addTo(map);
			marker.bindPopup(`${placeName} - ${formatLatitude(latitude)} - ${formatLongitude(longitude)}`);
			map.on('click', moveLocation);
			marker.on('dragend', newLocation);
		} else {
			map.on('click', addLocation);
		}
	}

	function addLocation(e) {
		newLatitude = e.latlng.lat;
		newLongitude = e.latlng.lng;
		marker = L.marker(e.latlng, { draggable: 'true', autoPan: 'true' }).addTo(map);
		marker.bindPopup(`${formatLatitude(newLatitude)} - ${formatLongitude(newLongitude)}`);
		map.off('click', addLocation);
		map.on('click', moveLocation);
		marker.on('dragend', newLocation);
	}

	function moveLocation(e) {
		marker.setLatLng(e.latlng);
		newLocation();
	}

	function newLocation() {
		newLatitude = marker.getLatLng().lat;
		newLongitude = marker.getLatLng().lng;
		marker.bindPopup(`${formatLatitude(newLatitude)} - ${formatLongitude(newLongitude)}`);
	}

	function formatLatitude(latitude) {
		const direction = latitude >= 0 ? 'N' : 'S';
		latitude = Math.abs(latitude);
		const degrees = Math.floor(latitude);
		latitude = (latitude - degrees) * 60;
		const minutes = Math.floor(latitude);
		latitude = (latitude - minutes) * 60;
		const seconds = latitude.toFixed(4);
		return `${degrees}°${minutes}'${seconds}" ${direction}`;
	}
	
	function formatLongitude(longitude){
		const direction = longitude >= 0 ? 'E' : 'W';
		longitude = Math.abs(longitude);
		const degrees = Math.floor(longitude);
		longitude = (longitude - degrees) * 60;
		const minutes = Math.floor(longitude);
		longitude = (longitude - minutes) * 60;
		const seconds = longitude.toFixed(4);
		return `${degrees}°${minutes}'${seconds}" ${direction}`;
	}
	
	function validateLocation(e){
		e.preventDefault();
		latitudeInput.value = newLatitude;
		longitudeInput.value = newLongitude;
		closeModal(e);
	}
});
