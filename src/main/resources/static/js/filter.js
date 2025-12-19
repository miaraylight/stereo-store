


function loadCategories(categories)
{
    const select = document.getElementById('category-select');

    categories.forEach(c => {
        const option = document.createElement('option');
        option.setAttribute('value', c.categoryId);
        option.innerText = c.name;
        select.appendChild(option);
    })
}

document.addEventListener('DOMContentLoaded', () => {
})

function clearFilters() {
    // 1. Reset the UI Elements
    document.getElementById('category-select').value = "0";
    document.getElementById('subcategory-select').value = "";

    const minPrice = document.getElementById('min-price');
    const maxPrice = document.getElementById('max-price');
    minPrice.value = 0;
    maxPrice.value = 2000;

    // 2. Reset the visual text spans
    document.getElementById('min-price-display').innerText = "0";
    document.getElementById('max-price-display').innerText = "2000";

    // 3. Tell ProductService to wipe its data and reload the products
    productService.clearAllFilters();
}
