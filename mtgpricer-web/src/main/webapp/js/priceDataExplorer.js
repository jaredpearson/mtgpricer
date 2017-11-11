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

function toggleCardSetTable(e, cardSetCode) {
    e.preventDefault();
    e.stopPropagation();

    // toggle the table
    const tableEl = document.querySelector(`[data-cardset-code="${cardSetCode}"] table`);
    const chevronEl = document.querySelector(`[data-cardset-code="${cardSetCode}"] span.chevron`);
    if (tableEl) {
        if (tableEl.classList.contains('hide')) {
            tableEl.classList.remove('hide');
            chevronEl.classList.remove('glyphicon-chevron-right');
            chevronEl.classList.add('glyphicon-chevron-down');
        } else {
            tableEl.classList.add('hide');
            chevronEl.classList.add('glyphicon-chevron-right');
            chevronEl.classList.remove('glyphicon-chevron-down');
        }
    }
}

function saveParserRules(e) {
    e.preventDefault();
    e.stopPropagation();
    
	var xhr = new XMLHttpRequest();
	xhr.onload = function(e) {
		if (this.status === 204) {
            alert('Parser rules saved');
		} else {
			alert('Failed to save parser rules');
			console.log(this.responseText);
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
