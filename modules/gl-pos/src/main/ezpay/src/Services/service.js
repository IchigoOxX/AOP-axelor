const headers = {
  Accept: 'application/json',
  'Content-Type': 'application/json',
  'X-Requested-With': 'XMLHttpRequest',
};

let lastCookieString;
let lastCookies = {};

function readCookie(name) {
  let cookieString = document.cookie || '';
  if (cookieString !== lastCookieString) {
    lastCookieString = cookieString;
    lastCookies = cookieString.split('; ').reduce((obj, value) => {
      let parts = value.split('=');
      obj[parts[0]] = parts[1];
      return obj;
    }, {});
  }
  return lastCookies[name];
}

class Service {
  constructor(props) {
    const baseURL = process.env.NODE_ENV === 'production' ? '../../' : '/axelor-erp/';
    this.state = {
      restURL: `${baseURL}ws/rest/`,
      actionURL: `${baseURL}ws/action/`,
      baseURL,
    };
  }

  getHeaders() {
    return {
      ...headers,
      'X-CSRF-Token': readCookie('CSRF-TOKEN'),
    };
  }

  doLogin(userName, password) {
    const url = `${this.state.baseURL}login.jsp`;
    const data = {
      username: userName,
      password: password,
    };
    return fetch(url, {
      method: 'POST',
      headers,
      credentials: 'include',
      body: JSON.stringify({ ...data }),
    }).then(res => {
      const token = res.headers.get('csrf-token');
      token && (document.cookie = `CSRF-TOKEN=${token}`);
      return res;
    });
  }

  async fetchProducts() {
    const url = `${this.state.restURL}com.axelor.apps.base.db.Product`;
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include',
    }).then(response => {
      return response.json();
    });
    /*const products = await response.json();
    return products;*/
  }
}

export default new Service();
