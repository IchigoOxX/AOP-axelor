import React, { useState } from "react";
import styled from "styled-components";
import Header from "./Header";
import Sidebar from "./Sidebar";

const Container = styled.div`
  width: ${(props) => (props.showSidebar ? "calc(100% - 220px)" : "100%")};
  padding-left: ${(props) => (props.showSidebar ? "220px" : "0")};
  transition: padding-right 0.3s ease;
  display: grid;
  grid-template-rows: auto 1fr;
  height: 100vh; /* Set container to full viewport height */
  overflow: hidden; /* Hide overflow initially */
`;
const Content = styled.div`
  position: relative;
  overflow: auto;
`;

const Page = (props) => {
  const [showSidebar, setShowSidebar] = useState(false);

  return (
    <>
      <Sidebar showSidebar={showSidebar} setShowSidebar={setShowSidebar} />
      <Container showSidebar={showSidebar}>
        <Header />
        <Content>{props.children}</Content>
      </Container>
    </>
  );
};

export default Page;
