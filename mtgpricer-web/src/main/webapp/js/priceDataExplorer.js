Handlebars.registerHelper({
    ge: function(v1, v2) {
        return v1 > v2;
    }
});

function convertMapToObject(map) {
    const result = {};
    for (let key of map.keys()) {
        result[key] = map.get(key);
    }
    return result;
}

class CardSetParserRules {
    constructor() {
        this.ignoredNames = new Set();
        this.numberOverrides = new Map();
        this.multiverseOverrides = new Map();
    }

    addIgnore(cardRawName) {
        this.ignoredNames.add(cardRawName);
    }

    addNumberOverride(cardRawName, cardNumber) {
        this.numberOverrides.set(cardRawName, cardNumber);
    }

    addMultiverseOverride(cardRawName, multiverseId) {
        this.multiverseOverrides.set(cardRawName, multiverseId);
    }

    delete(cardRawName) {
        this.ignoredNames.delete(cardRawName);
        this.numberOverrides.delete(cardRawName);
        this.multiverseOverrides.delete(cardRawName);
    }

    toStandardObject() {
        const result = {};
        if (this.ignoredNames.size > 0) {
            result.ignoredNames = Array.from(this.ignoredNames);
        }
        if (this.numberOverrides.size > 0) {
            result.numberOverrides = convertMapToObject(this.numberOverrides);
        }
        if (this.multiverseOverrides.size > 0) {
            result.multiverseOverrides = convertMapToObject(this.multiverseOverrides);
        }
        return result;
    }
}

class ParserRules {
    constructor() {
        this.ignoredSets = new Set();
        this.sets = new Map();
    }

    ensureCardSet(cardSetCode) {
        if (!this.sets.has(cardSetCode)) {
            this.sets.set(cardSetCode, new CardSetParserRules());
        }
        return this.sets.get(cardSetCode);
    }

    removeCardParserRule(cardSetCode, cardRawName) {
        if (!this.sets.has(cardSetCode)) {
            return;
        }
        this.sets.get(cardSetCode).delete(cardRawName);
        // TODO check to see if the cardSet is empty
    }

    /**
     * Converts the ParserRules into a standard object which can be converted
     * to JSON correctly.
     */
    toStandardObject() {
        const result = {};
        if (this.ignoredSets.size > 0) {
            result.ignoredSets = Array.from(this.ignoredSets);
        }
        if (this.sets.size > 0) {
            result.sets = {};
            for (let cardSetCode of this.sets.keys()) {
                result.sets[cardSetCode] = this.sets.get(cardSetCode).toStandardObject();
            }
        }
        return result;
    }
}

const newParserRules = new ParserRules();

function onCardActionChange(e, cardSetCode, cardRawName) {
    e.preventDefault();
    e.stopPropagation();

    // assume e.target is the <select> element that triggered the change
    var action = e.target.value;

    // make sure the value isn't already added to the parser rules
    newParserRules.removeCardParserRule(cardSetCode, cardRawName);

    if (action === '_') {
        // no action selected
    } else if (action === '_ignore') {
        newParserRules.ensureCardSet(cardSetCode).addIgnore(cardRawName);
    } else if (action.startsWith('number_')) {
        newParserRules.ensureCardSet(cardSetCode).addNumberOverride(cardRawName, action.substring(7));
    } else if (action.startsWith('multiverse_')) {
        const multiverseAsNumber = parseInt(action.substring(11));
        if (isNaN(multiverseAsNumber)) {
            throw new Error(`Multiverse number within action is not a number.\naction: ${action}\ncardSetCode: ${cardSetCode}\ncardRawName: ${cardRawName}`);
        }
        newParserRules.ensureCardSet(cardSetCode).addMultiverseOverride(cardRawName, multiverseAsNumber);
    } else {
        throw new Error(`Unknown or unsupported action selected: ${action}\ncardSetCode: ${cardSetCode}, cardRawName: ${cardRawName}`);
    }
}

// TODO move this template out of this JS file and precompile it
const unusedCardRowTemplate = Handlebars.compile(`<tr data-raw-name="{{unknownCard.rawName}}">
<td class="col-md-8">{{unknownCard.rawName}}</td>
<td class="col-md-4">
    <select name="action[{{unknownCard.rawName}}]" onchange="javascript: onCardActionChange(event, '{{cardSet.code}}', '{{unknownCard.rawName}}')">
        <option value="_">No Action</option>
        <option value="_ignore">Ignore</option>
        {{#if (ge cardSet.unusedCards.length 0)}}
            <option value="_"></option>
        {{/if}}
        {{#each cardSet.unusedCards}}
            {{#if number}}
                <option value="number_{{number}}">{{name}} ({{number}})</option>
            {{else}}
                <option value="multiverse_{{multiverseId}}">{{name}}</option>
            {{/if}}
        {{/each}}
    </select>
</td>
</tr>`);

function toggleCardSetTable(e, cardSetCode) {
    e.preventDefault();
    e.stopPropagation();

    // toggle the table loading
    const cardSetEl = document.querySelector(`[data-cardset-code="${cardSetCode}"]`);
    const chevronEl = document.querySelector(`[data-cardset-code="${cardSetCode}"] span.chevron`);

    const tableContainerEl = document.querySelector(`[data-cardset-code="${cardSetCode}"] .table-container`);
    const expanded = cardSetEl.getAttribute('data-expanded');
    if (!expanded) {
        chevronEl.classList.remove('glyphicon-chevron-right', 'spinning');
        chevronEl.classList.add('glyphicon-chevron-down');
        tableContainerEl.classList.remove('hide');
        cardSetEl.setAttribute('data-expanded', 'expanded');
    } else {
        chevronEl.classList.add('glyphicon-chevron-right', 'spinning');
        chevronEl.classList.remove('glyphicon-chevron-down');
        tableContainerEl.classList.add('hide');
        cardSetEl.removeAttribute('data-expanded');
    }
    
    // load the card information if it hasn't been loaded already
    if (!cardSetEl.getAttribute('data-loaded')) {
        cardSetEl.setAttribute('data-loaded', 'loading');
        chevronEl.classList.remove('glyphicon-chevron-down', 'glyphicon-chevron-right');
        chevronEl.classList.add('glyphicon-refresh', 'spinning');

        fetchCardSet(cardSetCode)
            .then(cardSet => {
                let rowsHtml = cardSet.unknownCards.reduce((out, unknownCard) => {
                    return out + unusedCardRowTemplate({cardSet, unknownCard})
                }, '');
                
                let tableEl = document.createElement('table');
                tableEl.classList.add('table', 'table-bordered');
                tableEl.style.marginBottom = '0px';
                tableEl.innerHTML = rowsHtml;

                // remove everything in the container
                while (tableContainerEl.firstChild) {
                    tableContainerEl.removeChild(tableContainerEl.firstChild);
                }

                // append the new table in the container
                tableContainerEl.appendChild(tableEl);
            })
            .then(() => {
                cardSetEl.setAttribute('data-loaded', 'loaded');
                chevronEl.classList.remove('glyphicon-refresh', 'spinning');
                
                if (cardSetEl.getAttribute('data-expanded')) {
                    chevronEl.classList.add('glyphicon-chevron-down');
                } else {
                    chevronEl.classList.add('glyphicon-chevron-right');
                }
            });

    }
}

function displayFatalError(message, details) {
    alert(message);
    if (details) {
        console.log(details);
    }
}

function fetchCardSet(cardSetCode) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.onload = function(e) {
            if (this.status === 200) {
                const cardSet = JSON.parse(this.responseText);
                resolve(cardSet);
            } else {
                reject('Failed to fetch card set information', this.responseText);
            }
        };
        xhr.open('GET', `/settings/priceDataExplorer/${priceDataId}/sets/${cardSetCode}.json`, true);
        xhr.send();
    });
}

function saveParserRules(e) {
    e.preventDefault();
    e.stopPropagation();
    
	const xhr = new XMLHttpRequest();
	xhr.onload = function(e) {
		if (this.status === 204) {
            alert('Parser rules saved');
		} else {
			displayFatalError('Failed to save parser rules', this.responseText);
		}
	};
	xhr.open('PATCH', '/settings/priceDataExplorer/cardkingdom/parserRules.json', true);
	xhr.send(JSON.stringify(newParserRules.toStandardObject()));
}

function downloadParserRules(e) {
    e.preventDefault();
    e.stopPropagation();

    window.location = '/settings/priceDataExplorer/cardkingdom/parserRules.json';
}

let priceDataId = undefined;
function startPriceDataExplorer(thisPriceDataId) {
    priceDataId = thisPriceDataId;
}