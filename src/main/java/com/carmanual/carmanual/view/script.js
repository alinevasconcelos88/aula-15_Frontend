const url = "http://localhost:8080/task/user/1";


function hideloader() {
    document.getElementById("loading").style.display = "none";
}


function show(tasks) {
    let tab = "";


    for (let task of tasks) {
        tab += `
            <tr>
            <td>${task.user.id}</td>
            <td>${task.user.username}</td>
            <td scope="row">${task.id}</td>  
            <td>${task.description}</td>
            </tr>`;
    }
    document.getElementById("tasks").innerHTML = tab;
}


async function getAPI(url) {
    try {
        const response = await fetch(url, { method: "GET" });
        const data = await response.json();
        console.log(data);


        if (response.ok) {
            hideloader();
            show(data);
        } else {
            console.error("Erro na requisição:", response.status);
        }
    } catch (error) {
        console.error("Erro de conexão:", error);
    }
}


getAPI(url);
