document.getElementById('openModalBtn').addEventListener('click', openModal);

//document.getElementById('closeModalBtn').addEventListener('click', closeModal);
document.querySelectorAll('.js-close-modal').forEach(a => {
	a.addEventListener('click', closeModal);
})

document.addEventListener('keydown', function(e){
	if (e.key === 'Escape'){
		closeModal(e);
	}
})

function openModal(e) {
	e.preventDefault();
	document.getElementById('modal').classList.remove('hidden');
}

function closeModal(e) {
	e.preventDefault();
	document.getElementById('modal').classList.add('hidden');
}