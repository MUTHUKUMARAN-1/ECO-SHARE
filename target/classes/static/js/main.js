/**
 * EcoNest — Client Interactivity
 */

document.addEventListener('DOMContentLoaded', function () {
  // 1. Auto-dismiss alerts
  const alerts = document.querySelectorAll('.alert');
  alerts.forEach(function (alert) {
    const closeBtn = alert.querySelector('.alert-close');
    if (closeBtn) {
      closeBtn.addEventListener('click', function () {
        alert.style.opacity = '0';
        setTimeout(() => alert.remove(), 250);
      });
    }
    // Auto fade after 6s
    setTimeout(function () {
      if (document.body.contains(alert)) {
        alert.style.transition = 'opacity 0.5s ease';
        alert.style.opacity = '0';
        setTimeout(() => alert.remove(), 500);
      }
    }, 6000);
  });

  // 2. Quantity steppers
  const steppers = document.querySelectorAll('.quantity-stepper');
  steppers.forEach(function (stepper) {
    const input = stepper.querySelector('.stepper-input');
    const minusBtn = stepper.querySelector('.stepper-btn.minus');
    const plusBtn = stepper.querySelector('.stepper-btn.plus');

    if (input && minusBtn && plusBtn) {
      const min = parseInt(input.getAttribute('min') || '1', 10);
      const max = parseInt(input.getAttribute('max') || '99', 10);

      minusBtn.addEventListener('click', function (e) {
        e.preventDefault();
        let val = parseInt(input.value, 10) || 1;
        if (val > min) {
          input.value = val - 1;
        }
      });

      plusBtn.addEventListener('click', function (e) {
        e.preventDefault();
        let val = parseInt(input.value, 10) || 1;
        if (val < max) {
          input.value = val + 1;
        }
      });
    }
  });

  // 3. Image error fallback
  const allImages = document.querySelectorAll('img');
  allImages.forEach(function (img) {
    img.addEventListener('error', function () {
      if (!img.dataset.fallbackApplied) {
        img.dataset.fallbackApplied = 'true';
        img.src = 'https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=800&q=80';
      }
    });
  });
});
