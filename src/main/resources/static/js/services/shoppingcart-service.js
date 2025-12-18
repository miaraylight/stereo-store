let cartService;

class ShoppingCartService {

    cart = {
        items:[],
        total:0
    };

    addToCart(productId)
    {
        const url = apiUrl(`/cart/products/${productId}`);
        // const headers = userService.getHeaders();

        axios.post(url, {})// ,{headers})
            .then(response => {
                this.setCart(response.data)

                this.updateCartDisplay()

            })
            .catch(error => {

                const data = {
                    error: "Add to cart failed."
                };

                templateBuilder.append("error", data, "errors")
            })
    }

    setCart(data)
    {
        this.cart = {
            items: [],
            total: 0
        }

        this.cart.total = data.total;

        for (const [key, value] of Object.entries(data.items)) {
            this.cart.items.push(value);
        }
    }

    loadCart()
    {

        const url = apiUrl(`/cart`);

        axios.get(url)
            .then(response => {
                this.setCart(response.data)

                this.updateCartDisplay()

            })
            .catch(error => {

                const data = {
                    error: "Load cart failed."
                };

                templateBuilder.append("error", data, "errors")
            })

    }

    loadCartPage()
    {
        // templateBuilder.build("cart", this.cart, "main");

        const main = document.getElementById("main")
        main.innerHTML = "";

        let div = document.createElement("div");
        div.classList="filter-box";
        main.appendChild(div);

        const contentDiv = document.createElement("div")
        contentDiv.id = "content";
        contentDiv.classList.add("content-form");

        const cartHeader = document.createElement("div")
        cartHeader.classList.add("cart-header")

        const h1 = document.createElement("h1")
        h1.innerText = "Cart";
        cartHeader.appendChild(h1);

        const button = document.createElement("button");
        button.classList.add("btn")
        button.classList.add("btn-danger")
        button.innerText = "Clear";
        button.addEventListener("click", () => this.clearCart());
        cartHeader.appendChild(button)

        contentDiv.appendChild(cartHeader)
        main.appendChild(contentDiv);
        const checkoutButton = document.createElement("button");
        checkoutButton.classList.add("btn", "btn-primary", "ms-2");
        checkoutButton.innerText = "Checkout";
        checkoutButton.addEventListener("click", () => this.loadCheckoutPage());
        cartHeader.appendChild(checkoutButton);
        // let parent = document.getElementById("cart-item-list");
        this.cart.items.forEach(item => {
            this.buildItem(item, contentDiv)
        });
    }

    buildItem(item, parent)
    {
        let outerDiv = document.createElement("div");
        outerDiv.classList.add("cart-item");

        let div = document.createElement("div");
        outerDiv.appendChild(div);
        let h4 = document.createElement("h4")
        h4.innerText = item.product.name;
        div.appendChild(h4);

        let photoDiv = document.createElement("div");
        photoDiv.classList.add("photo")
        let img = document.createElement("img");
        img.src = `/images/products/${item.product.imageUrl}`
        img.addEventListener("click", () => {
            showImageDetailForm(item.product.name, img.src)
        })
        photoDiv.appendChild(img)
        let priceH4 = document.createElement("h4");
        priceH4.classList.add("price");
        priceH4.innerText = `$${item.product.price}`;
        photoDiv.appendChild(priceH4);
        outerDiv.appendChild(photoDiv);

        let descriptionDiv = document.createElement("div");
        descriptionDiv.innerText = item.product.description;
        outerDiv.appendChild(descriptionDiv);

        let quantityDiv = document.createElement("div")
        quantityDiv.innerText = `Quantity: ${item.quantity}`;
        outerDiv.appendChild(quantityDiv)


        parent.appendChild(outerDiv);
    }

    clearCart()
    {

        const url = apiUrl(`/cart`);

        axios.delete(url)
             .then(response => {
                 this.cart = {
                     items: [],
                     total: 0
                 }

                 this.cart.total = response.data.total;

                 for (const [key, value] of Object.entries(response.data.items)) {
                     this.cart.items.push(value);
                 }

                 this.updateCartDisplay()
                 this.loadCartPage()

             })
             .catch(error => {

                 const data = {
                     error: "Empty cart failed."
                 };

                 templateBuilder.append("error", data, "errors")
             })
    }

    updateCartDisplay()
    {
        try {
            const itemCount = this.cart.items.length;
            const cartControl = document.getElementById("cart-items")

            cartControl.innerText = itemCount;
        }
        catch (e) {

        }
    }

    loadCheckoutPage() {
        const main = document.getElementById("main");
        main.innerHTML = "";

        const contentDiv = document.createElement("div");
        contentDiv.classList.add("content-form");

        // Header
        const headerDiv = document.createElement("div");
        headerDiv.classList.add("page-header");
        const h1 = document.createElement("h1");
        h1.innerText = "Checkout";
        headerDiv.appendChild(h1);
        contentDiv.appendChild(headerDiv);

        // Form
        const form = document.createElement("form");
        form.id = "checkoutForm";

        // Fields: address, city, state, zip
        const fields = ["address", "city", "state", "zip"];
        fields.forEach(field => {
            const formGroup = document.createElement("div");
            formGroup.classList.add("mb-3");

            const label = document.createElement("label");
            label.classList.add("form-label");
            label.setAttribute("for", field);
            label.innerText = field.charAt(0).toUpperCase() + field.slice(1);
            formGroup.appendChild(label);

            const input = document.createElement("input");
            input.type = "text";
            input.classList.add("form-control");
            input.id = field;
            input.name = field;

            formGroup.appendChild(input);
            form.appendChild(formGroup);
        });

        // Optional: Shipping Amount
        const shippingGroup = document.createElement("div");
        shippingGroup.classList.add("mb-3");
        const shippingLabel = document.createElement("label");
        shippingLabel.classList.add("form-label");
        shippingLabel.setAttribute("for", "shippingAmount");
        shippingLabel.innerText = "Shipping Amount";
        shippingGroup.appendChild(shippingLabel);

        const shippingInput = document.createElement("input");
        shippingInput.type = "number";
        shippingInput.classList.add("form-control");
        shippingInput.id = "shippingAmount";
        shippingInput.name = "shippingAmount";
        shippingInput.value = "0.00";
        shippingGroup.appendChild(shippingInput);
        form.appendChild(shippingGroup);

        // Place Order Button
        const buttonDiv = document.createElement("div");
        buttonDiv.classList.add("d-flex", "justify-content-end");
        const placeOrderBtn = document.createElement("button");
        placeOrderBtn.type = "button";
        placeOrderBtn.classList.add("btn", "btn-success");
        placeOrderBtn.innerText = "Place Order";
        placeOrderBtn.addEventListener("click", () => this.placeOrder());
        buttonDiv.appendChild(placeOrderBtn);

        form.appendChild(buttonDiv);
        contentDiv.appendChild(form);
        main.appendChild(contentDiv);

        // Optional: Pre-fill from profile
        profileService.getProfile().then(profile => {
            if(profile) {
                document.getElementById("address").value = profile.address;
                document.getElementById("city").value = profile.city;
                document.getElementById("state").value = profile.state;
                document.getElementById("zip").value = profile.zip;
            }
        });
    }

    placeOrder() {
        const url = apiUrl("/orders");

        const orderData = {
            address: document.getElementById("address").value,
            city: document.getElementById("city").value,
            state: document.getElementById("state").value,
            zip: document.getElementById("zip").value,
            shippingAmount: parseFloat(document.getElementById("shippingAmount").value)
        };

        axios.post(url, orderData)
            .then(response => {
                alert("Order placed successfully!");
                this.cart = { items: [], total: 0 };
                this.updateCartDisplay();
                this.loadCartPage(); // Optional: redirect to cart or orders page
            })
            .catch(error => {
                console.error(error);
                alert("Failed to place order. Please try again.");
            });
    }


}





document.addEventListener('DOMContentLoaded', () => {
    cartService = new ShoppingCartService();

    if(userService.isLoggedIn())
    {
        cartService.loadCart();
    }

});
