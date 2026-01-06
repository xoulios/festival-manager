import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";


export default function ProtectedRoute({ children, requiredRoles }: {
children: JSX.Element;
requiredRoles?: string[];
}) {
const { user } = useAuth();


if (!user) return <Navigate to="/login" replace />;
if (requiredRoles && !requiredRoles.includes(user.role)) return <Navigate to="/unauthorized" replace />;


return children;
}