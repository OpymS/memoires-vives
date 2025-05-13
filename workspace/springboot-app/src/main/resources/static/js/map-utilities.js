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

export {formatLatitude, formatLongitude};