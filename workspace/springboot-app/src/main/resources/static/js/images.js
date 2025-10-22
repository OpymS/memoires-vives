document.addEventListener('DOMContentLoaded', () => {
	const form = document.getElementById('formWithImage');
	const imageInput = document.getElementById('imageInput');
	const statusDiv = document.getElementById('upload-status');
	
	const MAX_FILE_SIZE = 1_999_000;
	const MAX_DIM = 1400;

	imageInput.addEventListener('change', function(e) {
		const file = e.target.files[0];
		const reader = new FileReader();

		reader.onload = function(e) {
			document.getElementById('image').src = e.target.result;
		};

		reader.readAsDataURL(file);
	});

	form.addEventListener('submit', async (e) => {
		const file = imageInput.files[0];
		if (!file) return;
		if (file.size < MAX_FILE_SIZE) return;

		e.preventDefault();

		try {
			statusDiv.classList.remove('hidden');
			console.log('redimensionnement de l\'image');
			const resizedFile = await resizeImage(file);
			await sendFormWithFile(form, imageInput, resizedFile);
		} catch (err) {
			console.error('Erreur pendant le redimensionnement :', err);
			form.submit();
		} finally {
			statusDiv.classList.add('hidden');
		}

	});

	async function resizeImage(file) {
		return new Promise((resolve, reject) => {
			const img = new Image();

			img.onerror = () => reject(new Error('Impossible de charger l’image'));

			img.onload = async () => {
				try {
					const canvas = document.createElement('canvas');

					let { width, height } = img;

					// Conserve le ratio
					if (width > MAX_DIM || height > MAX_DIM) {
						const ratio = Math.min(MAX_DIM / width, MAX_DIM / height);
						width *= ratio;
						height *= ratio;
					}

					canvas.width = width;
					canvas.height = height;

					const p = window.pica();

					await p.resize(img, canvas);

					let quality = 1.00;
					let blob;
					let resizedFile;

					do {
						blob = await p.toBlob(canvas, file.type, quality);
						resizedFile = new File([blob], file.name, { type: file.type });

						if (resizedFile.size <= MAX_FILE_SIZE || quality <= 0.5) break;

						quality -= 0.05;

					} while (true);


					console.log(`Image redimensionnée : ${file.size} → ${resizedFile.size} octets`);

					resolve(resizedFile);
				} catch (err) {
					reject(err);
				}
			};
			img.src = URL.createObjectURL(file);
		});
	}

	async function sendFormWithFile(form, imageInput, resizedFile) {
		const dataTransfer = new DataTransfer();
		dataTransfer.items.add(resizedFile);
		imageInput.files = dataTransfer.files;

		form.submit();
	}
});
