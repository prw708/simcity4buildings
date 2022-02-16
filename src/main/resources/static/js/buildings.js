const CARDS_PER_ROW = 4;

class SearchBar extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			update: null
		};
		this.debounceChange = this.debounceChange.bind(this);
		this.handleChange = this.handleChange.bind(this);
	}
	
	debounceChange(event) {
		this.props.onSearching(true);
		return function() {
			clearTimeout(this.state.update);
			this.setState({
				update: setTimeout(function() {
					this.setState({ update: null });
					this.handleChange.apply(this, [event]);
					this.props.onSearching(false);
				}.bind(this), 2000)
			});
		}.bind(this);
	}
	
	handleChange(event) {
		this.props.onSearchBarTextChange(event.target.value);
	}
	
	render() {
		return React.createElement("form", { className: "mb-4" }, 
			React.createElement("input", { 
				type: "text", 
				className: "form-control", 
				autoComplete: "off", 
				maxLength: 200, 
				value: this.props.searchBarText, 
				onChange: (e) => this.debounceChange(e)()
			}, null)
		);
	}
}

class OrderBy extends React.Component {
	constructor(props) {
		super(props);
	}
	
	handleClick(event, type) {
		event.preventDefault();
		this.props.onOrderByChange(type);
	}
	
	render() {
		return React.createElement("div", {
			className: "d-flex flex-row align-items-center mb-4"
			}, 
				React.createElement("span", { 
					className: "small m-0 me-4 me-sm-2 me-lg-4", 
				}, "Order By"),
				React.createElement("a", { 
					className: "btn btn-link btn-sm link-dark m-0 p-0 ms-4 ms-sm-2 ms-md-4",
					href: "#", 
					onClick: (event) => this.handleClick(event, "lastUpdated")
				}, "Last Updated"),
				React.createElement("a", { 
					className: "btn btn-link btn-sm link-dark m-0 p-0 ms-4 ms-sm-2 ms-md-4", 
					href: "#", 
					onClick: (event) => this.handleClick(event, "occupancy")
				}, "Occupancy"),
				React.createElement("a", { 
					className: "btn btn-link btn-sm link-dark m-0 p-0 ms-4 ms-sm-2 ms-md-4", 
					href: "#", 
					onClick: (event) => this.handleClick(event, "name")
				}, "Name")
		);
	}
}

class BuildingList extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		const cards = [];
		if (this.props.loading) {
			return React.createElement("div", { className: "text-center mb-4" }, 
				React.createElement("div", { className: "spinner-border" }, 
					React.createElement("span", { className: "visually-hidden" }, "Loading...")
				)
			);
		} else if (!this.props.buildings || this.props.buildings.length === 0) {
			return React.createElement("p", { className: "mb-4" }, "No buildings to show.");
		} else {
			this.props.buildings.forEach((building) => {
				var imageUrl = "/images/building_image.png";
				if (building.image) {
					imageUrl = "data:image/png;base64," + building.image;
				}
				var card = React.createElement("div", { className: "col", key: building.id }, 
					React.createElement("div", { className: "card h-100 position-relative" }, 
							React.createElement("img", { className: "card-img-top", src: imageUrl, alt: building.name }, null),
							React.createElement("div", { className: "card-body"}, 
								React.createElement("h5", { className: "card-title mb-3" }, building.name),
								React.createElement("small", { className: "card-subtitle mb-0" }, "Occupancy: " + building.occupancy)
							),
							React.createElement("div", { className: "card-footer bg-white"}, 
								React.createElement("a", { 
									className: "card-link link-secondary text-decoration-none", 
									href: "/view/" + building.id
								}, "View Details")
							)
					)
				);
				cards.push(card);
			});
			var scrolling = null;
			if (this.props.scrolling) {
				scrolling = React.createElement("div", { className: "text-center my-4" }, 
					React.createElement("div", { className: "spinner-border" }, 
						React.createElement("span", { className: "visually-hidden" }, "Loading...")
					)
				);
			}
			return React.createElement("div", {}, 
				React.createElement("div", {
					className: "row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3 mt-4"
				}, 
					cards,
				),
				scrolling
			)

		}
	}
}

class BuildingContainer extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			searchBarText: "", 
			orderBy: "lastUpdated",
			buildings: null,
			loading: true,
			scrolling: false,
			searching: false
		};
		this.inv = false;
		this.size = CARDS_PER_ROW;
		this.handleSearching = this.handleSearching.bind(this);
		this.handleSearchBarTextChange = this.handleSearchBarTextChange.bind(this);
		this.handleOrderByChange = this.handleOrderByChange.bind(this);
		this.handleScroll = this.handleScroll.bind(this);
	}
	
	handleSearching(searching) {
		this.setState({
			searching: searching
		});
	}
	
	handleSearchBarTextChange(text) {
		this.setState({
			loading: true
		});
		this.getSize()
			.then(function(size) {
				var viewableSize;
				if (!text) {
					viewableSize = CARDS_PER_ROW;
					this.size = CARDS_PER_ROW;
				} else {
					viewableSize = size;
				}
				return this.getAllBuildings(this.state.orderBy, viewableSize);
			}.bind(this))
			.then(function(allBuildings) {
				if (text) {
					var filteredBuildings = allBuildings.filter( (building) => building.name.toLowerCase().includes(text.toLowerCase()) );
					this.setState({
						searchBarText: text,
						buildings: filteredBuildings,
						loading: false
					});
				} else {
					this.setState({
						searchBarText: text,
						buildings: allBuildings,
						loading: false
					});
				}
			}.bind(this))
			.catch(function(err) {
				this.setState({
					loading: false
				});
			}.bind(this));
	}
	
	handleOrderByChange(type) {
		this.setState({
			loading: true,
			orderBy: type
		});
		if (type === this.state.orderBy) {
			this.inv = !this.inv;
		} else {
			this.inv = false;
		}
		this.size = CARDS_PER_ROW;
		this.getAllBuildings(type, this.size)
			.then(function(allBuildings) {
				this.setState({
					loading: false,
					buildings: allBuildings
				});
			}.bind(this))
			.catch(function(err) {
				this.setState({
					loading: false
				});
			}.bind(this));
	}
	
	handleScroll() {
		if (this.state.loading || this.state.scrolling || this.state.searching) {
			return false;
		}
		if ((Math.ceil(window.innerHeight) + Math.ceil(window.scrollY) >= Math.ceil(document.body.scrollHeight)) || 
			(Math.ceil(window.innerHeight) + Math.ceil(window.pageYOffset) >= Math.ceil(document.body.scrollHeight))) {
			this.getSize()
				.then(function(buildingsSize) {
					if (this.size < buildingsSize && !this.state.searchBarText) {
						this.setState({
							scrolling: true
						});
						this.size += CARDS_PER_ROW;
					} else {
						this.size = buildingsSize;
					}
					return this.getAllBuildings(this.state.orderBy, this.size);
				}.bind(this))
				.then(function(allBuildings) {
					if (this.state.searchBarText) {
						var filteredBuildings = allBuildings.filter( (building) => building.name.toLowerCase().includes(this.state.searchBarText.toLowerCase()) );
						this.setState({
							buildings: filteredBuildings,
							loading: false,
							scrolling: false
						});
					} else {
						this.setState({
							buildings: allBuildings,
							loading: false,
							scrolling: false
						});
					}
				}.bind(this))
				.catch(function(err) {
					this.setState({
						scrolling: false
					});
				}.bind(this));
		}
	}
	
	componentDidMount() {
		window.addEventListener("scroll", this.handleScroll);
		this.setState({
			loading: true
		});
		this.getAllBuildings(this.state.orderBy, this.size)
			.then(function(allBuildings) {
				this.setState({
					loading: false
				});
			}.bind(this))
			.catch(function(err) {
				this.setState({
					loading: false
				});
			}.bind(this));
	}
	
	componentWillUnmount() {
		window.removeEventListener("scroll", this.handleScroll);
	}
	
	getSize() {
		return new Promise(function(resolve, reject) {
			var httpRequest = new XMLHttpRequest();
			var path = "/all";
			httpRequest.open("GET", path, true);
			httpRequest.setRequestHeader("X-Requested-With", "XMLHttpRequest");
			httpRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
			httpRequest.send();
			httpRequest.onreadystatechange = function() {
				if (httpRequest.readyState === XMLHttpRequest.DONE) {
					if (httpRequest.status === 200) {
						var buildingsSize = JSON.parse(httpRequest.responseText);
						resolve(buildingsSize);
					} else {
						reject(httpRequest.status);
					}
				}
			}.bind(this);
		}.bind(this));
	}
	
	getAllBuildings(type, size) {
		return new Promise(function(resolve, reject) {
			var httpRequest = new XMLHttpRequest();
			var path = "/buildings";
			if (this.inv) {
				path += "?size=" + size.toString() + "&sort=" + type + ",asc";
			} else {
				path += "?size=" + size.toString() + "&sort=" + type + ",desc";
			}
			httpRequest.open("GET", path, true);
			httpRequest.setRequestHeader("X-Requested-With", "XMLHttpRequest");
			httpRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
			httpRequest.send();
			httpRequest.onreadystatechange = function() {
				if (httpRequest.readyState === XMLHttpRequest.DONE) {
					if (httpRequest.status === 200) {
						var buildings = JSON.parse(httpRequest.responseText);
						this.setState({
							buildings: buildings
						});
						resolve(buildings);
					} else {
						reject(httpRequest.status);
					}
				}
			}.bind(this);
		}.bind(this));
	}
	
	render() {
		return React.createElement("div", {}, 
			React.createElement(SearchBar, { onSearchBarTextChange: this.handleSearchBarTextChange, onSearching: this.handleSearching }, null),
			React.createElement(OrderBy, { onOrderByChange: this.handleOrderByChange }, null),
			React.createElement(BuildingList, { buildings: this.state.buildings, loading: this.state.loading, scrolling: this.state.scrolling }, null)
		);
	}
}

const domContainer = document.getElementById("buildingsContainer");
ReactDOM.render(
	React.createElement(BuildingContainer, {}, null), 
	domContainer
);