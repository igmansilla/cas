import React from "react";
import MainLayout from "./components/MainLayout";
import PackingListApp from "./components/PackingListApp";
// Assuming index.css is imported here for global styles as per the task description example
// If it's in main.tsx, this line might not be necessary.
// import './index.css'; 

function App() {
  return (
    <MainLayout>
      <PackingListApp />
    </MainLayout>
  );
}

export default App;
