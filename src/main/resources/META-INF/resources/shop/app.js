/**
 * Quarkus Merch shop — client-side cart + checkout (SUMMIT-1847 demo).
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  DEMO FIX for SUMMIT-1847                                           │
 * │                                                                     │
 * │  Checkout currently fails with an error popup.                      │
 * │  Flip CHECKOUT_BUG to false to apply the fix and showcase success.  │
 * └─────────────────────────────────────────────────────────────────────┘
 */
const CHECKOUT_BUG = true; // ← set to false to fix the checkout error

const PRODUCT = {
  id: "quarkus-rocks-tee",
  name: "“Quarkus rocks” Tee",
  unitPrice: 29,
};

const state = {
  qty: 0,
};

const els = {
  addToCart: document.getElementById("addToCart"),
  openCartFromProduct: document.getElementById("openCartFromProduct"),
  cartToggle: document.getElementById("cartToggle"),
  cartBadge: document.getElementById("cartBadge"),
  cartPanel: document.getElementById("cartPanel"),
  cartBackdrop: document.getElementById("cartBackdrop"),
  closeCart: document.getElementById("closeCart"),
  cartLines: document.getElementById("cartLines"),
  cartEmpty: document.getElementById("cartEmpty"),
  cartFooter: document.getElementById("cartFooter"),
  cartTotal: document.getElementById("cartTotal"),
  checkout: document.getElementById("checkout"),
  errorModal: document.getElementById("errorModal"),
  errorBody: document.getElementById("errorBody"),
  errorHint: document.getElementById("errorHint"),
  dismissError: document.getElementById("dismissError"),
  successModal: document.getElementById("successModal"),
  dismissSuccess: document.getElementById("dismissSuccess"),
};

function formatMoney(amount) {
  return `€${amount.toFixed(2)}`;
}

function renderCart() {
  const empty = state.qty === 0;
  els.cartEmpty.hidden = !empty;
  els.cartFooter.hidden = empty;
  els.cartLines.innerHTML = "";

  if (state.qty > 0) {
    const li = document.createElement("li");
    li.className = "cart-line";
    li.innerHTML = `
      <span class="cart-line-title">${PRODUCT.name}</span>
      <span class="cart-line-price">${formatMoney(PRODUCT.unitPrice * state.qty)}</span>
      <span class="cart-line-meta">Qty ${state.qty} · ${formatMoney(PRODUCT.unitPrice)} each</span>
    `;
    els.cartLines.appendChild(li);
    els.cartTotal.textContent = formatMoney(PRODUCT.unitPrice * state.qty);
  }

  if (state.qty === 0) {
    els.cartBadge.hidden = true;
  } else {
    els.cartBadge.hidden = false;
    els.cartBadge.textContent = String(state.qty);
  }
}

function openCart() {
  els.cartPanel.classList.add("is-open");
  els.cartPanel.setAttribute("aria-hidden", "false");
  els.cartToggle.setAttribute("aria-expanded", "true");
  els.cartBackdrop.hidden = false;
}

function closeCart() {
  els.cartPanel.classList.remove("is-open");
  els.cartPanel.setAttribute("aria-hidden", "true");
  els.cartToggle.setAttribute("aria-expanded", "false");
  els.cartBackdrop.hidden = true;
}

function addToCart() {
  state.qty += 1;
  renderCart();
  openCart();
}

/**
 * Simulated checkout. Intentionally broken while CHECKOUT_BUG is true
 * so presenters can flip one flag and re-test in the browser.
 */
function checkout() {
  if (state.qty === 0) {
    return;
  }

  // --- buggy path (SUMMIT-1847) -----------------------------------------
  if (CHECKOUT_BUG) {
    showError(
      "Payment gateway returned HTTP 500 while finalizing the order.",
      "shop/app.js → CHECKOUT_BUG = true  (set to false to fix)"
    );
    return;
  }

  // --- fixed path -------------------------------------------------------
  state.qty = 0;
  renderCart();
  closeCart();
  els.successModal.hidden = false;
}

function showError(message, hint) {
  els.errorBody.textContent = message;
  els.errorHint.textContent = hint;
  els.errorModal.hidden = false;
}

function dismissError() {
  els.errorModal.hidden = true;
}

function dismissSuccess() {
  els.successModal.hidden = true;
}

els.addToCart.addEventListener("click", addToCart);
els.openCartFromProduct.addEventListener("click", openCart);
els.cartToggle.addEventListener("click", () => {
  if (els.cartPanel.classList.contains("is-open")) {
    closeCart();
  } else {
    openCart();
  }
});
els.closeCart.addEventListener("click", closeCart);
els.cartBackdrop.addEventListener("click", closeCart);
els.checkout.addEventListener("click", checkout);
els.dismissError.addEventListener("click", dismissError);
els.dismissSuccess.addEventListener("click", dismissSuccess);

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    dismissError();
    dismissSuccess();
    closeCart();
  }
});

renderCart();
