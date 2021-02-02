import {Rxios} from "rxios";

export const http = new Rxios({
    // all regular axios request configuration options are valid here
    // check https://github.com/axios/axios#request-config
    baseURL: 'https://jsonplaceholder.typicode.com',
});
