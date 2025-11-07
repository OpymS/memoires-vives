document.addEventListener('DOMContentLoaded', () => {
  const tsInput = document.getElementById('formTimestamp');
  if (tsInput) {
    tsInput.value = Date.now();
  }
});