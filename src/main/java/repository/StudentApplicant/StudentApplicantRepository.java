package repository.StudentApplicant;

import model.dto.Student.PersonDTO;
import model.dto.Student.StudentApplicantDto;
import service.DBConnector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentApplicantRepository {

    public static boolean saveData(StudentApplicantDto dto) {
        Connection conn = DBConnector.getConnection();
        if (conn == null) {
            System.out.println("Lidhja me bazën e të dhënave nuk është e mundur.");
            return false;
        }

        String query = "INSERT INTO tblShkollaMesme (emriShkolles, piketMat, piketGjSh, piketAng, piketZgjedhore,lendaZgjedhore,suksesiKl10,suksesiKl11,suksesiKl12,certifikataNotave,leternjoftimi,diplomaShkolles) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, dto.getSchoolName());
            statement.setInt(2, dto.getMathPoints());
            statement.setInt(3, dto.getAlbanianPoints());
            statement.setInt(4, dto.getEnglishPoints());
            statement.setInt(5, dto.getChoosenSubPoints());
            statement.setString(6, dto.getChoosenSub());
            statement.setDouble(7, dto.getSuccesGrade10());
            statement.setDouble(8, dto.getSuccesGrade11());
            statement.setDouble(9, dto.getSuccesGrade12());
            statement.setBytes(10, convertFileToBytes(dto.getFileCertificate()));
            statement.setBytes(11, convertFileToBytes(dto.getFileIdentification()));
            statement.setBytes(12, convertFileToBytes(dto.getFileDiploma()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Të dhënat u ruajtën me sukses.");
                return true;
            } else {
                System.out.println("Nuk u arrit të ruajë të dhënat.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Gabim gjatë ruajtjes së të dhënave: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Gabim gjatë mbylljes së lidhjes me bazën e të dhënave: " + e.getMessage());
            }
        }
    }

    private static byte[] convertFileToBytes(File file) {
        if (file == null) return null;
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            System.out.println("Gabim gjatë konvertimit të skedarit në bajtë: " + e.getMessage());
            return null;
        }
    }

   public static PersonDTO SearchByPersonalNumber(String personalNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersonDTO person = null;

        try {

            conn = DBConnector.getConnection();
            String query = "SELECT * FROM tblperson WHERE numriPersonal = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, personalNumber);
            rs = ps.executeQuery();


            if (rs.next()) {
                person = new PersonDTO();
                person.setPersonalNumber(rs.getString("numriPersonal"));
                person.setName(rs.getString("emri"));
                person.setLastName(rs.getString("mbiemri"));
                person.setNationality(rs.getString("nacionaliteti"));
                person.setCity(rs.getString("qyteti"));
                person.setCountry(rs.getString("shteti"));
                person.setGender(rs.getString("gjinia"));
                person.setBirthDate(rs.getDate("dataLindjes"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return person;
    }
}
