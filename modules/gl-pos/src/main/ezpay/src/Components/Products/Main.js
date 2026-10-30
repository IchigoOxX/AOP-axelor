import React, {useEffect, useState} from 'react';
import ProductsList from './ProductsList';
import styled from 'styled-components';
import Categories from './Categories';
import CartPanel from '../CartPanel/CartPanel';
import FilterComponent from './FilterComponent';
import SearchComponent from '../Common/SearchComponent';
import {useDispatch, useSelector} from 'react-redux';
import OpenShift from '../Shifts/OpenShift';
import useScanDetection from "use-scan-detection";
import {addToCart} from "../Redux/cartSlice";

const Popup = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.15);
  color: black;
  z-index: 1;
`;
const Container = styled.div`
  display: grid;
  grid-template-columns: 2.5fr 1fr;
`;
const ItemsContainer = styled.div`
  position: relative;
  margin: 0 20px;
`;
const Div = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 5px;
  margin-top: 10px;
`;
const SearchAndFilter = styled.div`
  right: 0;
  display: flex;
  gap: 5px;
  margin-top: 10px;
`;
const BarcodeScan = styled.div`
display: none;
`

const Main = () => {
  const dispatch = useDispatch();
  const products = useSelector(state => state.products.products.list) || [];
  const currentShift = useSelector(state => state.shifts.currentShift);

  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [filters, setFilters] = useState({
    newArrivals: false,
    onPromotion: false,
    bestSellers: false,
    inStock: false,
  });
  const [sortOrder, setSortOrder] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  const [barcodeScan, setBarcodeScan] = useState("No barcode");
  const [scanKey, setScanKey] = useState(0); // Unique key for each scan

  useScanDetection({
    onComplete: (scanResult) => {
      setBarcodeScan(scanResult);
      setScanKey((prevKey) => prevKey + 1); // Update the scan key
    },
  });

  useEffect(() => {
    const cleanedBarcode = barcodeScan.replace(/Shift/g, "").trim();
    console.log("clean: ", cleanedBarcode);
    const product = products.find((p) => p.barcode === cleanedBarcode); // Find product by barcode
    if (product) {
      dispatch(addToCart({ product })); // Add product to cart
    }
  }, [scanKey]); // Depend on scanKey instead of barcodeScan

  const applyFiltersAndSort = () => {
    let filteredProducts = [...products];
    if (selectedCategory !== 'ALL') {
      filteredProducts = filteredProducts.filter(product => product.productCategory?.name === selectedCategory);
    }

    if (filters.newArrivals) {
      const currentDate = new Date();
      const cutoffDate = new Date();
      cutoffDate.setMonth(currentDate.getMonth() - 3);
      filteredProducts = filteredProducts.filter((product) => {
        if (!product.arrivalDate) {
          return false; // Exclude products with no arrival date
        }
        const arrivalDate = new Date(product.arrivalDate);
        return arrivalDate > cutoffDate && !isNaN(arrivalDate);
      });
    }
    if (filters.onPromotion) {
      filteredProducts = filteredProducts.filter(
        (product) => product.discountAvailable || product.offerAvailable
      );
    }
    if (filters.bestSellers) {
      filteredProducts = filteredProducts
          .filter(product => product.totalSold > 0)
          .sort((a, b) => b.totalSold - a.totalSold)
          .slice(0, 10);
    }
    if (filters.inStock) {
      filteredProducts = filteredProducts.filter(
        (product) => product.stock > 0
      );
    }
    if (searchQuery.trim() !== '') {
      const query = searchQuery.trim().toLowerCase();
      filteredProducts = filteredProducts.filter(product => product.name?.toLowerCase().includes(query));
    }
    if (sortOrder) {
      filteredProducts = filteredProducts.sort((a, b) => {
        if (sortOrder === "asc") {
          return a.salePrice - b.salePrice;
        } else if (sortOrder === "desc") {
          return b.salePrice - a.salePrice;
        }
        return 0;
      });
    }
    return filteredProducts;
  };

  return (
    <>
      {!currentShift && (
        <Popup>
          <OpenShift />
        </Popup>
      )}
      <Container>
        <ItemsContainer>
          <Div>
            <div>
              <Categories selectedCategory={selectedCategory} setSelectedCategory={setSelectedCategory} />
            </div>
            <SearchAndFilter>
              <SearchComponent setSearchQuery={setSearchQuery} />
              <FilterComponent
                filters={filters}
                setFilters={setFilters}
                sortOrder={sortOrder}
                setSortOrder={setSortOrder}
              />
            </SearchAndFilter>
          </Div>
          <BarcodeScan>{barcodeScan}</BarcodeScan>
          <ProductsList products={applyFiltersAndSort()} />
        </ItemsContainer>
        <CartPanel />
      </Container>
    </>
  );
};

export default Main;
