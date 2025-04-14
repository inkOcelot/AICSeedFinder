import axios from 'axios'

const apiClient = axios.create({
    baseURL: "http://127.0.0.1:2641",
    timeout: 0,
    headers: { "Content-Type": "application/json" }
})

const getPath = () => {
    return apiClient.get("/path")
}

const searchSeeds = (data) => {
    return apiClient.post("/seedfinder", data)
}

export {
    getPath,
    searchSeeds
}