import { formatLatitude, formatLongitude } from "./map-utilities.js";

document.addEventListener('DOMContentLoaded', () => {
	const latContainer = document.getElementById('latitude');
	const longContainer = document.getElementById('longitude');

	const locationName = document.getElementById('locationName').dataset.location_name;
	const country = document.getElementById('locationCountry').dataset.location_country;
	const city = document.getElementById('locationCity').dataset.location_city;
	const latitude = latContainer.dataset.latitude;
	const longitude = longContainer.dataset.longitude;

	const formatedLatitude = formatLatitude(latitude);
	const formatedLongitude = formatLongitude(longitude);

	latContainer.textContent = formatedLatitude;
	longContainer.textContent = formatedLongitude;

	let map = L.map("map", {
		worldCopyJump: true,
		zoom: 10,
		center: [latitude, longitude]
	});

	L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
		minZoom: 1,
		maxZoom: 20,
		attribution: 'données © <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
	}).addTo(map);
	const marker = L.marker([latitude, longitude]);
	marker.addTo(map);
	marker.bindPopup(`<div class="flex flex-col align-center"><h5 class="text-center"> ${locationName}</h5><h6 class="text-center">${city} - ${country}</h6><div class="text-center font-light">${formatedLatitude} - ${formatedLongitude}</div></div>`);

	setTimeout(() => map.invalidateSize(), 200);
});
