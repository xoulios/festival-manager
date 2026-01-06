import React from "react";
import { Link } from "react-router-dom";

interface Festival {
  id: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  state: string;
}

type ProgramCardProps = {
  program: Festival;
  manageMode?: boolean;
};

const ProgramCard: React.FC<ProgramCardProps> = ({
  program,
  manageMode = false,
}) => {
  const formatDate = (isoDate: string) => {
    return new Date(isoDate).toLocaleDateString();
  };

  let badgeColor = "bg-gray-100 text-gray-800";
  if (program.state === "ANNOUNCED") badgeColor = "bg-green-100 text-green-800";
  else if (program.state === "CREATED") badgeColor = "bg-gray-200 text-gray-800";
  else if (program.state === "SUBMISSION") badgeColor = "bg-blue-100 text-blue-800";
  else if (program.state === "REVIEW") badgeColor = "bg-yellow-100 text-yellow-800";
  else if (program.state === "SCHEDULING" || program.state === "DECISION")
    badgeColor = "bg-purple-100 text-purple-800";

  return (
    <div className="bg-white shadow-md rounded-lg p-4">
      <h2 className="text-xl font-semibold mb-2">{program.title}</h2>

      <p className="text-sm text-gray-600 mb-1">
        {formatDate(program.startDate)} – {formatDate(program.endDate)}
      </p>

      <p className="text-gray-700 text-sm mb-2 line-clamp-2">
        {program.description}
      </p>

      <span
        className={`inline-block ${badgeColor} text-xs font-medium px-2.5 py-0.5 rounded mb-3`}
      >
        {program.state}
      </span>

      <div>
        <Link
          to={`/programs/${program.id}`}
          className="text-indigo-600 hover:text-indigo-800 font-medium"
        >
          {manageMode ? "Διαχείριση" : "Προβολή"} »
        </Link>
      </div>
    </div>
  );
};

export default ProgramCard;
