document.addEventListener("DOMContentLoaded", function () {
    const container = document.getElementById("ingredients-container");
    const addButton = document.getElementById("add-button");

    let index = 1; // 最初の入力欄は index 0

    addButton.addEventListener("click", function () {
        const row = document.createElement("div");
        row.className = "ingredient-row";

        row.innerHTML = `
            <label>材料名:</label>
            <input type="text" name="ingredients[${index}].name" required>
            <label>使用量:</label>
            <input type="number" step="1" name="ingredients[${index}].quantity" required min="0">
            <label>単位:</label>
            <input type="text" name="ingredients[${index}].unit" required>
            <button type="button" class="remove-button">削除</button>
        `;

        container.appendChild(row);

        // 削除ボタンの処理を付与
        row.querySelector(".remove-button").addEventListener("click", function () {
            container.removeChild(row);
        });

        index++;
    });
});
