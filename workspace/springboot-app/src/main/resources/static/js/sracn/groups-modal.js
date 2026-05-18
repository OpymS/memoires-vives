window.openGroupModal = function(btn) {
	const modal = document.getElementById("groupModal");

	const title = document.getElementById("modalTitle");
	const description = document.getElementById("modalDescription");
	const age = document.getElementById("modalAge");
	const level = document.getElementById("modalLevel");

	const objectivesList = document.getElementById("modalObjectives");
	const scheduleList = document.getElementById("modalSchedule");

	if (!modal || !title || !description) {
		console.error("Modal non initialisée correctement");
		return;
	}

	// TEXTES SIMPLES
	title.innerText = btn.dataset.title;
	description.innerText = btn.dataset.description;
	age.innerText = btn.dataset.age;
	level.innerText = btn.dataset.level;

	// OBJECTIFS
	objectivesList.innerHTML = "";
	btn.dataset.objectives.split(";").map(o => o.trim()).forEach(o => {
		const li = document.createElement("li");
		li.innerText = o;
		objectivesList.appendChild(li);
	});

	// HORAIRES
	const schedules = btn.dataset.schedule.split(";");
	scheduleList.innerHTML = "";
	schedules.forEach((s, index) => {
		const [type, day, time] = s.split("|");

		const li = document.createElement("li");

		if (type === "OR") {
			li.innerText = `${day} ${time}` + (index < schedules.length - 1 ? "  OU" : "");
		} else {
			li.innerText = `${day} ${time}` + (index < schedules.length - 1 ? "  ET" : "");
		}

		scheduleList.appendChild(li);
	});

	// SHOW MODAL
	modal.classList.remove("hidden");
	modal.classList.add("flex");
	document.body.classList.add("overflow-hidden");
};

window.closeGroupModal = function() {
	const modal = document.getElementById("groupModal");

	modal.classList.add("hidden");
	modal.classList.remove("flex");

	document.body.classList.remove("overflow-hidden");
};

document.addEventListener("keydown", function(e) {
	if (e.key === "Escape") {
		closeGroupModal();
	}
});