window.openGroupModal = function(btn) {
	const overlay = document.getElementById("modalOverlay");
	const content = document.getElementById("modalContent");
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
	scheduleList.innerHTML = "";

	const schedules = btn.dataset.schedule.split(";");

	schedules.forEach((s, index) => {

		const [type, day, time] = s.split("|");

		// CHIP
		const chip = document.createElement("div");

		chip.className =
			"px-3 py-2 rounded-full text-sm font-medium " +
			"bg-emerald-100 text-emerald-800";

		chip.innerText = `${day} ${time}`;

		scheduleList.appendChild(chip);

		// AJOUT DU ET / OU ENTRE LES CHIPS
		if (index < schedules.length - 1) {

			const separator = document.createElement("div");

			separator.className =
				"flex items-center justify-center mx-1 " +
				"text-xs uppercase tracking-wide font-bold " +
				"text-(--color-sracn-muted)";

			separator.innerText = type === "OR" ? "OU" : "ET";

			scheduleList.appendChild(separator);
		}
	});

	// active modal
	modal.classList.remove("pointer-events-none");
	modal.classList.add("opacity-100");

	// animate overlay
	overlay.classList.remove("opacity-0");
	overlay.classList.add("opacity-100");

	// animate content
	content.classList.remove("opacity-0", "scale-95", "translate-y-4");
	content.classList.add("opacity-100", "scale-100", "translate-y-0");

	document.body.classList.add("overflow-hidden");
};

window.closeGroupModal = function() {

	const modal = document.getElementById("groupModal");
	const overlay = document.getElementById("modalOverlay");
	const content = document.getElementById("modalContent");

	// animate overlay
	overlay.classList.remove("opacity-100");
	overlay.classList.add("opacity-0");

	// animate content
	content.classList.remove("opacity-100", "scale-100", "translate-y-0");
	content.classList.add("opacity-0", "scale-95", "translate-y-4");

	// hide modal after animation
	setTimeout(() => {
		modal.classList.remove("opacity-100");
		modal.classList.add("pointer-events-none");
	}, 300);

	document.body.classList.remove("overflow-hidden");
};

document.addEventListener("keydown", function(e) {
	if (e.key === "Escape") {
		closeGroupModal();
	}
});