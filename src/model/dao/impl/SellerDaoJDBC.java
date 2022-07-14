package model.dao.impl;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellerDaoJDBC implements SellerDao {
    // crio um objeto Connection como uma atributo
    private Connection conn;
// crio um construtor usando o Connection. Assim faço a injeção de dependencia
    public SellerDaoJDBC(Connection conn){
        this.conn = conn;
    }

    @Override
    public void insert(Seller obj) {

    }

    @Override
    public void update(Seller obj) {

    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public Seller findById(Integer id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try{
            st=conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName "
                    +"FROM seller INNER JOIN department "
                    +"ON seller.DepartmentId = department.Id "
                    +"WHERE seller.Id = ?");
            st.setInt(1,id);
            rs = st.executeQuery();
            //testar se veio algum resultado. Se vier criamos os objetos Seller e department
            if(rs.next()){
                //Pegando os dados para preencher o obj Dep
                Department dep = instantiateDepartment(rs);
                // //Pegando os dados para preencher o obj Seller
                Seller obj = instantiateSeller(rs, dep);
                return obj;
            }
            //caso nao retorne nada, significa que nao tenho um vendedor no ID
            return null;
        }catch (SQLException e){
            throw new DbException(e.getMessage());
        }
        finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }

    }

    private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
      Seller obj =  new Seller();
        obj.setId(rs.getInt("Id"));
        obj.setName(rs.getString("Name"));
        obj.setEmail(rs.getString("Email"));
        obj.setBaseSalary(rs.getDouble("BaseSalary"));
        obj.setBirthDate(rs.getDate("BirthDate"));
        obj.setDepartment(dep);
        return obj;
    }

    private Department instantiateDepartment(ResultSet rs) throws  SQLException{
      Department dep =  new Department();
        dep.setId(rs.getInt("DepartmentId"));
        dep.setName(rs.getString("DepName"));
        return dep;
    }

    @Override
    public List<Seller> findAll() {
        return null;
    }

    @Override
    public List<Seller> findByDepartament(Department departament) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName "
                            + "FROM seller INNER JOIN department "
                            + "ON seller.DepartmentId = department.Id "
                            + "WHERE DepartmentId = ? "
                            + "ORDER BY Name");
            st.setInt(1, departament.getId());

            rs = st.executeQuery();
            //Lista para armazenar os resultados
            List<Seller> list = new ArrayList<>();
            // Map vazio para guardar qualquer departamento que eu instanciar
            Map<Integer, Department> map = new HashMap<>();
            //Enquanto tiver resultados percorra meu resultSet

            while (rs.next()) {
                //Toda vez que passar pelo meu While, eu tento buscar o ID do departamento no Map para saber se ja existe ou nao
                //Se o Map retornar nulo, ai sim vou instanciar o departamento. Caso contrario o departamento ja existe
                Department dep = map.get(rs.getInt("DepartmentId"));

                if(dep == null){
                   dep = instantiateDepartment(rs);
                   map.put(rs.getInt("DepartmentId"), dep);
               }
                Seller obj = instantiateSeller(rs, dep);
                list.add(obj);
            }
            return list;
        }
        catch (SQLException e){
            throw new DbException(e.getMessage());
        }
        finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }
}
