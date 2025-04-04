document.addEventListener('DOMContentLoaded', () => {

	const init = () => {
		const latitudeInput = document.getElementById('latitude');
		const longitudeInput = document.getElementById('longitude');
		const placeNameInput = document.getElementById('locationName');

		const latitude = parseFloat(latitudeInput.value) || 0;
		const longitude = parseFloat(longitudeInput.value) || 0;
		const placeName = placeNameInput.value;

		let userLat = 0;
		let userLong = 0;
		let newLatitude = latitude;
		let newLongitude = longitude;
		let newPlaceName = placeName;

		let map;
		let marker;
		
		let mapFirstOpening = true;


		initModal();
		initMap(latitude, longitude, placeName);

		function openModal(e) {
			e.preventDefault();
			document.getElementById('modal').classList.remove('hidden');
			
			if (placeName !== '') {
				if (mapFirstOpening){
					marker = createMarker(latitude, longitude, placeName);
					mapFirstOpening = false;
				}
				map.on('click', moveLocation);
			} else {
				map.on('click', addLocation);
				map.panTo(new L.LatLng(userLat, userLong));
			}
			
			setTimeout(() => map.invalidateSize(), 200);
		}

		function closeModal(e, id) {
			e.preventDefault();
			document.getElementById(id).classList.add('hidden');
		}

		function initModal() {
			document.getElementById('openModalBtn').addEventListener('click', openModal);

			document.querySelectorAll('.js-close-modal').forEach(a => {
				a.addEventListener('click', e => closeModal(e, 'modal'));
			});

			document.addEventListener('keydown', function(e) {
				if (e.key === 'Escape') {
					closeModal(e, 'modal');
				}
			});

			document.getElementById('validateLocation').addEventListener('click', validateLocation);
		}

		function initMap(latitude, longitude, placeName) {

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
		}
		
		function createMarker(lat, long, name){
			const marker = L.marker([lat, long], {draggable: 'true', autoPan: 'true'}).addTo(map);
			marker.bindPopup(`<input id="placeNameChange" type="text" value="${name}"/><div>${formatLatitude(lat)} - ${formatLongitude(long)}</div>`);
			marker.on('dragend', newLocation);
			marker.on('popupopen', changeName);
			return marker;
		}

		function addLocation(e) {
			newLatitude = e.latlng.lat;
			newLongitude = e.latlng.lng;
			marker = createMarker(newLatitude, newLongitude, newPlaceName);
			map.off('click', addLocation);
			map.on('click', moveLocation);
		}

		function moveLocation(e) {
			marker.setLatLng(e.latlng);
			newLocation();
		}

		function newLocation() {
			newLatitude = marker.getLatLng().lat;
			newLongitude = marker.getLatLng().lng;
			marker.bindPopup(`<input id="placeNameChange" type="text" value="${newPlaceName}" placeholder="Donnez un nom à ce lieu"/><div>${formatLatitude(newLatitude)} - ${formatLongitude(newLongitude)}</div>`);
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

		function formatLongitude(longitude) {
			const direction = longitude >= 0 ? 'E' : 'W';
			longitude = Math.abs(longitude);
			const degrees = Math.floor(longitude);
			longitude = (longitude - degrees) * 60;
			const minutes = Math.floor(longitude);
			longitude = (longitude - minutes) * 60;
			const seconds = longitude.toFixed(4);
			return `${degrees}°${minutes}'${seconds}" ${direction}`;
		}

		function validateLocation(e) {
			e.preventDefault();
			if (newPlaceName === '') {
				giveAName(e);
			} else {
				latitudeInput.value = newLatitude;
				longitudeInput.value = newLongitude;
				placeNameInput.value = newPlaceName;
				console.log(placeNameInput.value);
				closeModal(e, 'modal');
			}
		}

		function changeName() {
			document.getElementById('placeNameChange').addEventListener('blur', function() {
				newPlaceName = this.value;
				marker.bindPopup(`<input id="placeNameChange" type="text" value="${newPlaceName}" placeholder="Donnez un nom à ce lieu"/><div>${formatLatitude(newLatitude)} - ${formatLongitude(newLongitude)}</div>`);
			});
		}

		function giveAName(e) {
			closeModal(e, 'modal');
			const modalName = document.getElementById('modalName');
			modalName.classList.remove('hidden');
			
			document.querySelectorAll('.js-close-modalName').forEach(a => {
				a.addEventListener('click', function(e) {
					closeModal(e, 'modalName');
					openModal(e);
				});
			});
			
			const buttonValName = document.getElementById('validateLocationName');
			const nameInput = document.getElementById('modalNameInput');
			nameInput.addEventListener('input', function() {
				if (nameInput.value !== '') {
					buttonValName.classList.remove('bg-[#e9e9ed]');
					buttonValName.classList.add('bg-[#bddab2]', 'hover:opacity-80');
					buttonValName.disabled = false;
				} else {
					buttonValName.classList.remove('bg-[#bddab2]', 'hover:opacity-80');
					buttonValName.classList.add('bg-[#e9e9ed]');
					buttonValName.disabled = true;
				}
			});
			
			buttonValName.addEventListener('click', validate)
		}

		function validate(e) {
			e.preventDefault();
			newPlaceName = document.getElementById('modalNameInput').value;
			closeModal(e, 'modalName');
			openModal(e);
			validateLocation(e);
		}
	}

	init();

});
