/**
 *
 * Utility functions for form input validation, cookie management, etc.
 *
 * @author Peter Smith
 */
'use strict';


(function() {
    
    const VERSION = '1.0.0',
          api = (fns) => {return Object.assign(Object.create(null), fns)},
          name = `/javascript/responsio-${VERSION}.js`,
          script = document.querySelector(`script[src$="${name}"]`);

    if (script) {

        let responsio = Object.create(null);
        responsio.api = api;

        /* Determine where this javascript came from */

        let root_url = new URL(script.getAttribute('src'), window.location.href).href.replace(new RegExp(`${name}`),'/'),
            identity = script.getAttribute('data-responsio-identity');

        responsio.cfg = api({url: api({root: root_url, service: `${root_url}responsio/`}), identity: identity});

        /* Finally, export the API */

        if (identity) window.RESPONSIO = responsio;
    }
})();




/****************************************************************** 
                        DOM

    DOM manipulation functions

******************************************************************/
if (window.RESPONSIO) {
    const responsio = window.RESPONSIO, serializer = new XMLSerializer();

    responsio.dom = responsio.api({
        fromString: (html) => {
            return document.createRange().createContextualFragment(html).firstElementChild;
        },

        escape: (text) => serializer.serializeToString(document.createTextNode(text)).replaceAll("\n", "<br/>")
    });

}



/****************************************************************** 
                        STORAGE

    Persistant storage

******************************************************************/
if (window.RESPONSIO) {
    const responsio = window.RESPONSIO, {api} = responsio;

    /**
     * Get a named value (using dotted notation or an array) from a dictionary.
     */
    const getValue = (dict, name, _default) => {
        const parts = Array.isArray(name) ? name : (name ? name.split('.') : []);
        return parts.reduce(
            (node, leaf) => ((node && typeof node == 'object' && !Array.isArray(node) && (leaf in node)) ? node[leaf] : _default), dict);
    };


    /**
     * Set a named dictionary value using dotted or array notation.
     */
    const setValue = (dict, name, value) => {
        const parts = Array.isArray(name) ? name : name.split('.');
        parts.slice(0, -1).reduce(
            (node, leaf) =>
                {if (!(leaf in node)) node[leaf] = Object.create(null); return node[leaf]},
            dict)[parts.pop()] = value;
        return dict;
    };


    /* Determine whether or not local storage is available */

    let get, set;

    try {
        window.localStorage.setItem('test', 'test');
        window.localStorage.removeItem('test');

        /* If we get here, local storage is available */

        let cache = JSON.parse(window.localStorage.getItem('com.paradoxwebsolutions.chatbot') || '{}');

        get = (name, _default) => getValue(cache, name, _default);
        set = (name, value) => {
            setValue(cache, name, value);
            window.localStorage.setItem('com.paradoxwebsolutions.chatbot', JSON.stringify(cache));
        };
    } catch(e) {
        /* Local storage not available */
        let cache = Object.create(null);
        get = (name, _default) => getValue(cache, name, _default);
        set = (name, value) => setValue(cache, name, value);
    }

    /* Publish API */

    responsio.storage = api({get: get, set: set});
}



/****************************************************************** 
                        NETWORK

    Functions for submitting HTTP requests to a backend server

******************************************************************/
if (window.RESPONSIO) {

    const responsio = window.RESPONSIO;

    /**
     * Callback invoked when a network call completes.
     *
     * @param pass  a callback to be invoked if the response status indicates success (20x)
     * @param fail  a callback to be invoked if the response status indicates failure (!= 20x)
     */
    const onLoad = (pass, fail) => {
        return (evt) => {
            let request = evt.currentTarget, 
                contentType = request.getResponseHeader('Content-Type') || 'application/json',
                response = request.response;

            contentType = contentType.split(";")[0];
            switch(contentType) {
                case 'application/json':
                    try {
                        response = JSON.parse(response);
                        if (request.status != 200 && request.status != 204) response = response.error || 'Internal Server Error';
                    }
                    catch (error) {
                        console.error('Failed parsing JSON response:', request.responseText);
                        response = 'Internal Error';
                    }
                    break;
                default: console.error("Unsupported content Type", contentType);
            }

            /* Execute the appropriate callback */

            if (request.status == 200 || request.status == 204)  {
                if (pass) pass(response)
            }
            else if(fail) fail(request.status, response);
        }
    };


    /**
     * Submits an HTTP post request to a given endpoint (server/url).
     *
     * @param endpoint  the URL to submit the request to
     * @param data      the data (object) to submit
     * @param pass      a callback to be invoked when the request succeeds
     * @param fail      a callback to be invoked if the request fails
     * @param options   additional call options for content/encoding the request
     */
    const post = (endpoint, data, pass, fail, options) => {
        const request = new XMLHttpRequest(),
            url = new URL(endpoint.startsWith('http') ? endpoint : (responsio.cfg.url.service + endpoint));

        request.withCredentials = true;
        request.addEventListener('load', onLoad(pass, fail));
        request.addEventListener('error', () => {if (fail) fail(500, 'Internal Error')});
        request.addEventListener('timeout', () => {if (fail) fail(500, 'Request Timeout')});
        request.open('POST', url.toString());
        request.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
        if (options) {
            if ('type' in options)
            request.setRequestHeader('Content-Type', options.type);
            if ('encoding' in options)
                request.setRequestHeader('Content-Transfer-Encoding', options.encoding);
        }
        request.send(JSON.stringify(data));
    };



    /**
     * Submits an HTTP get request to a given endpoint (server/url).
     *
     * @param endpoint  the URL to submit the request to
     * @param params    a dictionary of any query parameters to include
     * @param pass      a callback to be invoked when the request succeeds
     * @param fail      a callback to be invoked if the request fails
     * @param options   additional call options for content/encoding the request
     */
    const get = (endpoint, params, pass, fail, options) => {
        const url = new URL(endpoint.startsWith('http') ? endpoint : (responsio.cfg.url.service + endpoint)),
            request = new XMLHttpRequest();
        
        if (params) Object.keys(params).forEach(k => url.searchParams.set(k, params[k]));

        request.withCredentials = true;
        request.addEventListener('load', onLoad(pass, fail));
        request.addEventListener('error', () => {if (fail) fail(500, 'Internal Error')});
        request.addEventListener('timeout', () => {if (fail) fail(500, 'Request Timeout')});
        request.open('GET', url.toString());
        if (options) {
            if ('accept' in options) request.setRequestHeader('Accept', options.accept);
        }
        request.send();
    };


    /* Publish API */

    responsio.network = responsio.api({post: post, get: get});
}



/****************************************************************** 
                        COMMANDS

    Command functions

******************************************************************/
if (window.RESPONSIO) {
    const responsio = window.RESPONSIO, {cfg, dom, network, storage} = responsio,
        cssId = 'pws-cb-style',
        winId = 'pws-cb-win',
        win = dom.fromString(`
            <div id="${winId}" class="pws-cb-win">
                <div class="pws-cb-handle"></div>
                <div class="pws-cb-title">Title</div>
                <div class="pws-cb-chat">
                </div>
                <div class="pws-cb-footer"><textarea placeholder="Enter your message..."></textarea></div>
            </div>`),
        handle = win.querySelector('.pws-cb-handle'),
        title = win.querySelector('.pws-cb-title'),
        chatWin = win.querySelector('.pws-cb-chat'),
        input = win.querySelector('textarea');


    /**
     * Initialize the responsio system. This involves loading the stylesheet, setting up the
     * responsio window, etc; This should only be invoked once, when the page is first loaded,
     * and as a response to an 'init' api call.
     *
     * @param options    the client configuration 
     */
    const init = (options) => {

        /* Install our configured stylesheets */

        const style = options.style || 'default',
            selector = options.selector || 'body',
            attachment = document.querySelector(selector);

        if (!attachment) {
            console.error("Responsio: Invalid window attachment point");
            return;
        }
        const head  = document.head,
            customStyles = `<link id="${cssId}" rel="stylesheet" type="text/css" href="${cfg.url.root}css/responsio-${style}.css"/>`,
            baseStyles = `<link id="${cssId}" rel="stylesheet" type="text/css" href="${cfg.url.root}css/responsio-base-styles.css"/>`;

        head.appendChild(dom.fromString(customStyles));
        const link = head.appendChild(dom.fromString(baseStyles));


        /* Create the responsio window */

        if (options.title) title.textContent = options.title;
        attachment.appendChild(win);


        /*
         * Add a handler to so that when the stylesheet is loaded we scroll the responsio window
         * to the bottom. This is because the history can be populated before the stylesheet
         * is loaded, and the unstyled client height will be different.
         */
        link.addEventListener('load', () => {
            chatWin.scrollTop = chatWin.scrollHeight - chatWin.clientHeight;
        });


        /*
         * Listen to clicks on the handle to open/close the responsio window itself;
         */
        handle.addEventListener('click', () => {
            win.classList.toggle('pws-cb-show');
        });


        /*
         * Add handler to input to detect <enter> and submit message.
         */
        input.addEventListener('keyup', (e) => {
            if (e.key == 'Enter' && input.value.trim().length > 0) {

                /* Get the text entered by the user and move it to a proper 'user' message in the scrolling history */

                const msg = input.value.trim(),
                    html = `<div class="pws-cb-message pws-cb-user"><span>${dom.escape(msg)}</span></div>`;
                input.value = '';
                chatWin.append(dom.fromString(html));
                chatWin.append(dom.fromString(`<div class="pws-cb-message pws-cb-bot pws-cb-bot-pending"></div>`));
                chatWin.scrollTop = chatWin.scrollHeight - chatWin.clientHeight;


                /* Add the message to the history storage */

                const history = storage.get('history', []);
                history.push(html);
                storage.set('history', history);


                /* Submit the message to the chabot service */

                network.post(
                    `chat/${cfg.identity}`,
                    {input: msg},
                    (response) => responsio.execute(response.commands),
                    (fail) => console.log(fail),
                    {accept: 'application/json'}
                );
            }
        });
    }



    /**
     * Command used to restore the last know state.
     */
    const restore = () => {
        const history = storage.get('history', []);

        /* Clean out all messages */

        while (chatWin.firstChild) chatWin.firstChild.remove();


        /* Repopulate the message history */

        history.forEach(html => chatWin.append(dom.fromString(html)));


        /* Scroll down to make sure message is visible */

        chatWin.scrollTop = chatWin.scrollHeight - chatWin.clientHeight;
    }



    /**
     * Command used to reset the cahtbot state.
     */
    const reset = () => {
        /* Clear any previous history */

        storage.set('history', []);

        /* Clean out all messages */

        while (chatWin.firstChild) chatWin.firstChild.remove();
    }



    /**
     * Output a response message from the chatbot.
     *
     * @param str   the text message to output
     */
    const text = (str) => {
        const html = `<div class="pws-cb-message pws-cb-bot"><span>${dom.escape(str)}</span></div>`;

        /* Remove pending block (there should only be one) */

        chatWin.querySelectorAll('.pws-cb-bot-pending').forEach(n => n.remove());


        /* Add in the new text message from the bot */

        chatWin.append(dom.fromString(html));


        /* Scroll down to make sure message is visible */

        chatWin.scrollTop = chatWin.scrollHeight - chatWin.clientHeight;


        /* Add the message to the history storage */

        const history = storage.get('history', []);
        history.push(html);
        storage.set('history', history);
    }


    /* Publish the API */

    responsio.commands = responsio.api({
        init: init,
        restore: restore,
        reset: reset,
        text: text
    });
}



/****************************************************************** 
                        EXECUTOR

    Command executor

******************************************************************/
if (window.RESPONSIO) {

    const responsio = window.RESPONSIO;

    responsio.execute = (list) => {
        if (Array.isArray(list)) {
            list.forEach(entry => {
                if (responsio.commands[entry.command]) {
                    responsio.commands[entry.command](entry.data);
                }
                else {
                    console.error('Invalid command');
                    console.error(entry);
                }
            });
        }
        else {
            console.error('Invalid response');
            console.error(list);
        }
    }
}



/****************************************************************** 
                        MAIN

    Triggers the initialization of the chat 

******************************************************************/

if (window.RESPONSIO) {
    const responsio = window.RESPONSIO;

    /* Submit initialization request */

    responsio.network.get(
        `init/${responsio.cfg.identity}`,
        null,
        (response) => responsio.execute(response.commands),
        null,
        {accept: 'application/json'}
    );
}